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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final List<String> ROLES_VALIDOS_ADMIN = List.of("veterinario", "admin");
    private static final String ROL_USER = "user";

    private final UsuarioRepository usuarioRepository;
    private final CentroVeterinarioRepository centroRepository;

    // POST /api/admin/usuarios
    @PostMapping("/usuarios")
    public ResponseEntity<?> crearUsuario(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CrearUsuarioDTO dto) {

        String token = extraerToken(authHeader);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Token no válido"));
        }
        String rolSesion = JwtUtil.obtenerRol(token);
        boolean esAdmin = "admin".equals(rolSesion);
        boolean esVeterinario = "veterinario".equals(rolSesion);

        if (!esAdmin && !esVeterinario) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tienes permiso para crear usuarios"));
        }

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El email ya está registrado"));
        }

        Integer idCentroAsignado;
        String rolAsignado;
        if (esAdmin) {
            if (!ROLES_VALIDOS_ADMIN.contains(dto.getRol())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Rol no válido. Roles permitidos: " + ROLES_VALIDOS_ADMIN));
            }
            if ("veterinario".equals(dto.getRol()) && dto.getIdCentro() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Un veterinario debe tener un centro asignado"));
            }
            idCentroAsignado = dto.getIdCentro();
            rolAsignado = dto.getRol();
        } else {
            // Veterinario: solo puede crear usuarios normales en su propio centro
            if (dto.getRol() != null && !ROL_USER.equals(dto.getRol())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Los veterinarios solo pueden crear usuarios normales"));
            }
            idCentroAsignado = JwtUtil.obtenerIdCentro(token);
            if (idCentroAsignado == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El veterinario no tiene un centro asignado"));
            }
            rolAsignado = ROL_USER;
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        usuario.setRol(rolAsignado);

        if (idCentroAsignado != null) {
            CentroVeterinario centro = centroRepository.findById(idCentroAsignado).orElse(null);
            if (centro == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Centro veterinario no encontrado con id: " + idCentroAsignado));
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
        if (token == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Token no válido"));
        }
        String rolSesion = JwtUtil.obtenerRol(token);
        boolean esAdmin = "admin".equals(rolSesion);
        boolean esVeterinario = "veterinario".equals(rolSesion);

        if (!esAdmin && !esVeterinario) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tienes permiso para listar usuarios"));
        }

        final Integer idCentroVet = esVeterinario ? JwtUtil.obtenerIdCentro(token) : null;

        List<Map<String, Object>> usuarios = usuarioRepository.findAll().stream()
                .filter(u -> {
                    if (esAdmin) return true;
                    if (!ROL_USER.equals(u.getRol())) return false;
                    Integer idCentroUsuario = u.getCentro() != null ? u.getCentro().getIdCentro() : null;
                    return idCentroVet != null && idCentroVet.equals(idCentroUsuario);
                })
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getIdUsuario());
                    m.put("nombre", u.getNombre());
                    m.put("email", u.getEmail());
                    m.put("rol", u.getRol());
                    m.put("idCentro", u.getCentro() != null ? u.getCentro().getIdCentro() : 0);
                    m.put("nombreCentro", u.getCentro() != null ? u.getCentro().getNombre() : "");
                    m.put("activo", u.getActivo());
                    m.put("telefono", u.getTelefono());
                    m.put("direccion", u.getDireccion());
                    m.put("monedas", u.getMonedas());
                    m.put("fechaRegistro", u.getFechaRegistro() != null ? u.getFechaRegistro().toString() : null);
                    return m;
                })
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
        if (token == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Token no válido"));
        }
        String rolSesion = JwtUtil.obtenerRol(token);
        boolean esAdmin = "admin".equals(rolSesion);
        boolean esVeterinario = "veterinario".equals(rolSesion);

        if (!esAdmin && !esVeterinario) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tienes permiso para editar usuarios"));
        }

        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }

        if (esVeterinario) {
            if (!ROL_USER.equals(usuario.getRol())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Solo puedes editar usuarios normales"));
            }
            Integer idCentroVet = JwtUtil.obtenerIdCentro(token);
            Integer idCentroUsuario = usuario.getCentro() != null ? usuario.getCentro().getIdCentro() : null;
            if (idCentroVet == null || !idCentroVet.equals(idCentroUsuario)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Solo puedes editar usuarios de tu centro"));
            }
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
        if (esAdmin && dto.getRol() != null) {
            if (!ROLES_VALIDOS_ADMIN.contains(dto.getRol())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Rol no válido. Roles permitidos: " + ROLES_VALIDOS_ADMIN));
            }
            usuario.setRol(dto.getRol());
        }
        if (esAdmin && dto.getIdCentro() != null) {
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
        if (token == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Token no válido"));
        }
        String rolSesion = JwtUtil.obtenerRol(token);
        boolean esAdmin = "admin".equals(rolSesion);
        boolean esVeterinario = "veterinario".equals(rolSesion);

        if (!esAdmin && !esVeterinario) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tienes permiso para eliminar usuarios"));
        }

        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }

        if (esVeterinario) {
            if (!ROL_USER.equals(usuario.getRol())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Solo puedes eliminar usuarios normales"));
            }
            Integer idCentroVet = JwtUtil.obtenerIdCentro(token);
            Integer idCentroUsuario = usuario.getCentro() != null ? usuario.getCentro().getIdCentro() : null;
            if (idCentroVet == null || !idCentroVet.equals(idCentroUsuario)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Solo puedes eliminar usuarios de tu centro"));
            }
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
