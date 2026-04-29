package com.pawlink.api.service;

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
import com.pawlink.api.model.Usuario;
import com.pawlink.api.repository.UsuarioRepository;
import com.pawlink.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UsuarioRepository usuarioRepository;

    private static final String CLIENT_ID =
            "101310526119-1kim0prkaj8k7de1r45699s3ummti3ji.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "OCULTO_POR_SEGURIDAD";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

    @Transactional
    public AuthResponseDTO loginConGoogle(GoogleLoginDTO dto) {
        try {
            NetHttpTransport transport = new NetHttpTransport();
            GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            boolean esFlujoCodigo = dto.getCode() != null && !dto.getCode().isEmpty();
            GoogleIdToken idToken = resolverIdToken(dto, transport, jsonFactory, esFlujoCodigo);

            if (idToken == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de Google inválido");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String nombre = (String) payload.get("name");
            String googleId = payload.getSubject();

            Usuario usuario = resolverUsuario(email, nombre, googleId, esFlujoCodigo);

            if (esFlujoCodigo && !"admin".equals(usuario.getRol()) && !"veterinario".equals(usuario.getRol())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Acceso restringido a administradores y veterinarios");
            }

            String token = JwtUtil.generarToken(usuario);
            return new AuthResponseDTO(token, usuario.getIdUsuario(), usuario.getNombre(), usuario.getRol(),
                    usuario.getCentro() != null ? usuario.getCentro().getIdCentro() : null);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al validar token de Google: " + e.getMessage());
        }
    }

    private GoogleIdToken resolverIdToken(GoogleLoginDTO dto, NetHttpTransport transport,
                                          GsonFactory jsonFactory, boolean esFlujoCodigo) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();

        if (esFlujoCodigo) {
            TokenResponse tokenResponse = new AuthorizationCodeTokenRequest(
                    transport, jsonFactory, new GenericUrl(TOKEN_URL), dto.getCode())
                    .setRedirectUri(REDIRECT_URI)
                    .setClientAuthentication(new BasicAuthentication(CLIENT_ID, CLIENT_SECRET))
                    .execute();

            String idTokenString = (String) tokenResponse.get("id_token");
            if (idTokenString == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google no devolvió id_token");
            }
            return verifier.verify(idTokenString);
        }

        if (dto.getIdToken() != null && !dto.getIdToken().isEmpty()) {
            return verifier.verify(dto.getIdToken());
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe enviar idToken o code");
    }

    // Busca el usuario por googleId o email; crea uno nuevo si no existe (flujo móvil)
    private Usuario resolverUsuario(String email, String nombre, String googleId, boolean esFlujoCodigo) {
        Usuario usuario = usuarioRepository.findByGoogleId(googleId)
                .orElseGet(() -> usuarioRepository.findByEmail(email).orElse(null));

        if (usuario == null) {
            if (esFlujoCodigo) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Usuario no autorizado. Contacte con el administrador.");
            }
            usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setEmail(email);
            usuario.setGoogleId(googleId);
            return usuarioRepository.save(usuario);
        }

        if (usuario.getGoogleId() == null) {
            usuario.setGoogleId(googleId);
            usuarioRepository.save(usuario);
        }

        return usuario;
    }
}
