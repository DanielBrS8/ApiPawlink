package com.pawlink.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.pawlink.api.model.Usuario;

import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "pawlink_dev_secret_key_2024";
    private static final long EXPIRATION_MS = 86_400_000; // 24 horas

    public static String generarToken(Usuario usuario) {
        var builder = JWT.create()
                .withSubject(usuario.getIdUsuario().toString())
                .withClaim("email", usuario.getEmail())
                .withClaim("nombre", usuario.getNombre())
                .withClaim("rol", usuario.getRol());

        if (usuario.getCentro() != null) {
            builder.withClaim("idCentro", usuario.getCentro().getIdCentro());
        }

        return builder
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .sign(Algorithm.HMAC256(SECRET));
    }

    public static DecodedJWT verificarToken(String token) {
        return JWT.require(Algorithm.HMAC256(SECRET)).build().verify(token);
    }

    public static String obtenerRol(String token) {
        return verificarToken(token).getClaim("rol").asString();
    }

    public static Integer obtenerId(String token) {
        return Integer.valueOf(verificarToken(token).getSubject());
    }

    public static Integer obtenerIdCentro(String token) {
        var claim = verificarToken(token).getClaim("idCentro");
        return claim.isMissing() ? null : claim.asInt();
    }
}
