package com.pawlink.api.controller;

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.pawlink.api.dto.AuthResponseDTO;
import com.pawlink.api.dto.GoogleLoginDTO;
import com.pawlink.api.dto.LoginRequestDTO;
import com.pawlink.api.dto.RegistroRequestDTO;
import com.pawlink.api.model.Usuario;
import com.pawlink.api.repository.UsuarioRepository;
import com.pawlink.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    private static final String GOOGLE_CLIENT_ID = "OCULTO_POR_SEGURIDAD";
    private static final String GOOGLE_CLIENT_SECRET = "OCULTO_POR_SEGURIDAD";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

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
                .body(new AuthResponseDTO(token, usuario.getIdUsuario(), usuario.getNombre(), usuario.getRol(), usuario.getCentro() != null ? usuario.getCentro().getIdCentro() : null));
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

        if (!"admin".equals(usuario.getRol()) && !"veterinario".equals(usuario.getRol())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso restringido a administradores y veterinarios"));
        }

        String token = JwtUtil.generarToken(usuario);
        return ResponseEntity.ok(new AuthResponseDTO(token, usuario.getIdUsuario(), usuario.getNombre(), usuario.getRol(), usuario.getCentro() != null ? usuario.getCentro().getIdCentro() : null));
    }

    // POST /api/auth/google
    // Acepta idToken (móvil/web) o code (escritorio)
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginDTO dto) {
        try {
            NetHttpTransport transport = new NetHttpTransport();
            GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            GoogleIdToken idToken;

            if (dto.getCode() != null && !dto.getCode().isEmpty()) {
                // Flujo authorization code (escritorio): intercambiar code por tokens
                TokenResponse tokenResponse = new AuthorizationCodeTokenRequest(
                        transport, jsonFactory, new GenericUrl(GOOGLE_TOKEN_URL), dto.getCode())
                        .setRedirectUri(GOOGLE_REDIRECT_URI)
                        .setClientAuthentication(new BasicAuthentication(GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET))
                        .execute();

                String idTokenString = (String) tokenResponse.get("id_token");
                if (idTokenString == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Google no devolvió id_token"));
                }

                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                        .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                        .build();
                idToken = verifier.verify(idTokenString);
            } else if (dto.getIdToken() != null && !dto.getIdToken().isEmpty()) {
                // Flujo directo (móvil/web): validar idToken recibido
                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                        .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                        .build();
                idToken = verifier.verify(dto.getIdToken());
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Debe enviar idToken o code"));
            }

            if (idToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token de Google inválido"));
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String nombre = (String) payload.get("name");
            String googleId = payload.getSubject();

            boolean esFlujoCodigo = dto.getCode() != null && !dto.getCode().isEmpty();

            Usuario usuario = usuarioRepository.findByGoogleId(googleId)
                    .orElseGet(() -> usuarioRepository.findByEmail(email).orElse(null));

            if (usuario == null) {
                if (esFlujoCodigo) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "Usuario no autorizado. Contacte con el administrador."));
                }
                // Móvil/Web: auto-registro permitido
                usuario = new Usuario();
                usuario.setNombre(nombre);
                usuario.setEmail(email);
                usuario.setGoogleId(googleId);
                usuarioRepository.save(usuario);
            } else if (usuario.getGoogleId() == null) {
                usuario.setGoogleId(googleId);
                usuarioRepository.save(usuario);
            }

            if (esFlujoCodigo && !"admin".equals(usuario.getRol()) && !"veterinario".equals(usuario.getRol())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso restringido a administradores y veterinarios"));
            }

            String token = JwtUtil.generarToken(usuario);
            return ResponseEntity.ok(new AuthResponseDTO(token, usuario.getIdUsuario(), usuario.getNombre(), usuario.getRol(), usuario.getCentro() != null ? usuario.getCentro().getIdCentro() : null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al validar token de Google: " + e.getMessage()));
        }
    }
}
