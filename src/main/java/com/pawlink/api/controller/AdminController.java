package com.pawlink.api.controller;

import com.pawlink.api.dto.AuthResponseDTO;
import com.pawlink.api.dto.CrearUsuarioDTO;
import com.pawlink.api.model.CentroVeterinario;
import com.pawlink.api.model.Usuario;
import com.pawlink.api.repository.CentroVeterinarioRepository;
import com.pawlink.api.repository.UsuarioRepository;
import com.pawlink.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final List<String> ROLES_VALIDOS = List.of("veterinario", "admin");

    private final UsuarioRepository usuarioRepository;
    private final CentroVeterinarioRepository centroRepository;

    // POST /api/admin/usuarios
    @PostMapping("/usuarios")
    public ResponseEntity<?> crearUsuario(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CrearUsuarioDTO dto) {

        String token = extraerToken(authHeader);
        if (token == null || !"admin".equals(JwtUtil.obtenerRol(token))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo administradores pueden crear usuarios"));
        }

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El email ya está registrado"));
        }

        if (!ROLES_VALIDOS.contains(dto.getRol())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Rol no válido. Roles permitidos: " + ROLES_VALIDOS));
        }

        if ("veterinario".equals(dto.getRol()) && dto.getIdCentro() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Un veterinario debe tener un centro asignado"));
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        usuario.setRol(dto.getRol());

        if (dto.getIdCentro() != null) {
            CentroVeterinario centro = centroRepository.findById(dto.getIdCentro())
                    .orElse(null);
            if (centro == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Centro veterinario no encontrado con id: " + dto.getIdCentro()));
            }
            usuario.setCentro(centro);
        }

        usuarioRepository.save(usuario);

        Integer idCentro = usuario.getCentro() != null ? usuario.getCentro().getIdCentro() : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponseDTO(null, usuario.getIdUsuario(), usuario.getNombre(), usuario.getRol(), idCentro));
    }

    // GET /api/admin/usuarios
    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuarios(
            @RequestHeader("Authorization") String authHeader) {

        String token = extraerToken(authHeader);
        if (token == null || !"admin".equals(JwtUtil.obtenerRol(token))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo administradores pueden listar usuarios"));
        }

        List<Map<String, Object>> usuarios = usuarioRepository.findAll().stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getIdUsuario(),
                        "nombre", u.getNombre(),
                        "email", u.getEmail(),
                        "rol", u.getRol(),
                        "idCentro", u.getCentro() != null ? u.getCentro().getIdCentro() : 0,
                        "nombreCentro", u.getCentro() != null ? u.getCentro().getNombre() : "",
                        "activo", u.getActivo()
                ))
                .toList();

        return ResponseEntity.ok(usuarios);
    }

    // PUT /api/admin/usuarios/{id}
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<?> actualizarUsuario(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer id,
            @RequestBody CrearUsuarioDTO dto) {

        String token = extraerToken(authHeader);
        if (token == null || !"admin".equals(JwtUtil.obtenerRol(token))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo administradores pueden editar usuarios"));
        }

        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }

        // Si cambia el email, verificar que no esté en uso por otro usuario
        if (dto.getEmail() != null && !dto.getEmail().equals(usuario.getEmail())
                && usuarioRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El email ya está registrado"));
        }

        if (dto.getNombre() != null) usuario.setNombre(dto.getNombre());
        if (dto.getEmail() != null) usuario.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            usuario.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        }
        if (dto.getRol() != null) {
            if (!ROLES_VALIDOS.contains(dto.getRol())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Rol no válido. Roles permitidos: " + ROLES_VALIDOS));
            }
            usuario.setRol(dto.getRol());
        }
        if (dto.getIdCentro() != null) {
            CentroVeterinario centro = centroRepository.findById(dto.getIdCentro()).orElse(null);
            if (centro == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Centro veterinario no encontrado con id: " + dto.getIdCentro()));
            }
            usuario.setCentro(centro);
        }

        usuarioRepository.save(usuario);

        Integer idCentro = usuario.getCentro() != null ? usuario.getCentro().getIdCentro() : null;
        return ResponseEntity.ok(
                new AuthResponseDTO(null, usuario.getIdUsuario(), usuario.getNombre(), usuario.getRol(), idCentro));
    }

    // DELETE /api/admin/usuarios/{id}
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> eliminarUsuario(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer id) {

        String token = extraerToken(authHeader);
        if (token == null || !"admin".equals(JwtUtil.obtenerRol(token))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo administradores pueden eliminar usuarios"));
        }

        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private String extraerToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
