package com.pawlink.api.controller;

import com.pawlink.api.dto.HistorialAdopcionesDTO;
import com.pawlink.api.dto.UsuarioDTO;
import com.pawlink.api.model.Usuario;
import com.pawlink.api.repository.UsuarioRepository;
import com.pawlink.api.security.JwtUtil;
import com.pawlink.api.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<?> listarUsuarios(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token no proporcionado"));
        }
        String token = authHeader.substring(7);
        String rol = JwtUtil.obtenerRol(token);
        if (!"admin".equals(rol) && !"veterinario".equals(rol)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No tienes permiso para listar usuarios"));
        }

        final Integer idCentroVet = "veterinario".equals(rol) ? JwtUtil.obtenerIdCentro(token) : null;

        List<Map<String, Object>> usuarios = usuarioRepository.findAll().stream()
                .filter(u -> {
                    if ("admin".equals(rol)) return true;
                    if (!"user".equals(u.getRol())) return false;
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
                    return m;
                })
                .toList();

        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}/historial-adopciones")
    public ResponseEntity<HistorialAdopcionesDTO> historialAdopciones(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.obtenerHistorialAdopciones(id));
    }

    // PUT /api/usuarios/{id} - actualizar perfil del usuario
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body) {

        // 1. Validar token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token no proporcionado"));
        }
        String token = authHeader.substring(7);
        Integer idSesion = JwtUtil.obtenerId(token);
        String rol = JwtUtil.obtenerRol(token);

        // 2. Buscar usuario
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }

        // 3. Permisos:
        //    - Cualquiera puede editar su propio perfil
        //    - Admin puede editar a cualquiera
        //    - Veterinario solo puede editar usuarios con rol "user" de su propio centro
        boolean esPropio = id.equals(idSesion);
        boolean esAdmin = "admin".equals(rol);
        boolean esVeterinario = "veterinario".equals(rol);

        boolean puedeEditar = esPropio || esAdmin;
        if (!puedeEditar && esVeterinario) {
            Integer idCentroVet = JwtUtil.obtenerIdCentro(token);
            Integer idCentroUsuario = usuario.getCentro() != null ? usuario.getCentro().getIdCentro() : null;
            boolean esUserDeMiCentro = "user".equals(usuario.getRol())
                    && idCentroVet != null
                    && idCentroVet.equals(idCentroUsuario);
            puedeEditar = esUserDeMiCentro;
        }

        if (!puedeEditar) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tienes permiso para editar este usuario"));
        }

        // 4. Actualizar campos (solo los que vienen en el body)
        if (body.get("nombre") != null) {
            usuario.setNombre(body.get("nombre").toString());
        }
        if (body.get("telefono") != null) {
            usuario.setTelefono(body.get("telefono").toString());
        }
        if (body.get("direccion") != null) {
            usuario.setDireccion(body.get("direccion").toString());
        }
        if (body.get("foto") != null) {
            usuario.setFotoPerfil(body.get("foto").toString());
        }

        // 5. Guardar
        usuarioRepository.save(usuario);

        // 6. Devolver el usuario actualizado
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("id", usuario.getIdUsuario());
        respuesta.put("nombre", usuario.getNombre());
        respuesta.put("email", usuario.getEmail());
        respuesta.put("telefono", usuario.getTelefono());
        respuesta.put("direccion", usuario.getDireccion());
        respuesta.put("foto", usuario.getFotoPerfil());
        respuesta.put("rol", usuario.getRol());
        respuesta.put("idCentro", usuario.getCentro() != null ? usuario.getCentro().getIdCentro() : null);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{id}/vincular/{idCentro}")
    public ResponseEntity<UsuarioDTO> vincularCentro(
            @PathVariable Integer id,
            @PathVariable Integer idCentro) {
        return ResponseEntity.ok(usuarioService.vincularCentro(id, idCentro));
    }
}
