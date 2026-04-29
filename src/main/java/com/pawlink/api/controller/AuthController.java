package com.pawlink.api.controller;

import com.pawlink.api.dto.AuthResponseDTO;
import com.pawlink.api.dto.GoogleLoginDTO;
import com.pawlink.api.dto.LoginRequestDTO;
import com.pawlink.api.dto.RegistroRequestDTO;
import com.pawlink.api.model.Usuario;
import com.pawlink.api.repository.UsuarioRepository;
import com.pawlink.api.security.JwtUtil;
import com.pawlink.api.service.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final GoogleAuthService googleAuthService;

    // POST /api/auth/registro
    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody RegistroRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El email ya está registrado"));
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));

        usuarioRepository.save(usuario);

        String token = JwtUtil.generarToken(usuario);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponseDTO(token, usuario.getIdUsuario(), usuario.getNombre(), usuario.getRol(),
                        usuario.getCentro() != null ? usuario.getCentro().getIdCentro() : null));
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail()).orElse(null);

        if (usuario == null || usuario.getPassword() == null
                || !BCrypt.checkpw(dto.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));
        }

        String token = JwtUtil.generarToken(usuario);
        return ResponseEntity.ok(new AuthResponseDTO(token, usuario.getIdUsuario(), usuario.getNombre(), usuario.getRol(),
                usuario.getCentro() != null ? usuario.getCentro().getIdCentro() : null));
    }

    // POST /api/auth/google — acepta idToken (móvil/web) o code (escritorio)
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginDTO dto) {
        AuthResponseDTO response = googleAuthService.loginConGoogle(dto);
        return ResponseEntity.ok(response);
    }
}
