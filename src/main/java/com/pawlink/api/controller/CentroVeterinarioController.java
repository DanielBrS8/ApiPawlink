package com.pawlink.api.controller;

import com.pawlink.api.model.CentroVeterinario;
import com.pawlink.api.repository.CentroVeterinarioRepository;
import com.pawlink.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/centros")
@RequiredArgsConstructor
public class CentroVeterinarioController {

    private final CentroVeterinarioRepository centroRepository;

    @GetMapping
    public ResponseEntity<List<CentroVeterinario>> getAll() {
        return ResponseEntity.ok(centroRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CentroVeterinario> getById(@PathVariable Integer id) {
        return centroRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CentroVeterinario centro) {

        String token = extraerToken(authHeader);
        if (token == null || !"admin".equals(JwtUtil.obtenerRol(token))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo administradores pueden crear centros"));
        }

        CentroVeterinario guardado = centroRepository.save(centro);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer id,
            @RequestBody CentroVeterinario datos) {

        String token = extraerToken(authHeader);
        if (token == null || !"admin".equals(JwtUtil.obtenerRol(token))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo administradores pueden editar centros"));
        }

        CentroVeterinario centro = centroRepository.findById(id).orElse(null);
        if (centro == null) {
            return ResponseEntity.notFound().build();
        }

        if (datos.getNombre() != null) centro.setNombre(datos.getNombre());
        if (datos.getCiudad() != null) centro.setCiudad(datos.getCiudad());
        if (datos.getDireccion() != null) centro.setDireccion(datos.getDireccion());
        if (datos.getTelefono() != null) centro.setTelefono(datos.getTelefono());
        if (datos.getEspecialidad() != null) centro.setEspecialidad(datos.getEspecialidad());
        if (datos.getFoto() != null) centro.setFoto(datos.getFoto());
        if (datos.getHorario() != null) centro.setHorario(datos.getHorario());
        if (datos.getLatitud() != null) centro.setLatitud(datos.getLatitud());
        if (datos.getLongitud() != null) centro.setLongitud(datos.getLongitud());

        return ResponseEntity.ok(centroRepository.save(centro));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer id) {

        String token = extraerToken(authHeader);
        if (token == null || !"admin".equals(JwtUtil.obtenerRol(token))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo administradores pueden eliminar centros"));
        }

        if (!centroRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        centroRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private String extraerToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
