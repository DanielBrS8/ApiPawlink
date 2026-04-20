package com.pawlink.api.controller;

import com.pawlink.api.dto.MascotaDTO;
import com.pawlink.api.dto.MascotaRequestDTO;
import com.pawlink.api.security.JwtUtil;
import com.pawlink.api.service.MascotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
public class MascotaController {

    private final MascotaService mascotaService;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Integer disponible,
            @RequestParam(required = false) Integer idCentro) {

        Integer centroFiltro = idCentro != null ? idCentro : obtenerIdCentroDesdeToken(authHeader);

        List<MascotaDTO> result;
        if (centroFiltro != null) {
            result = mascotaService.findByCentro(centroFiltro);
        } else if (disponible != null) {
            result = mascotaService.findDisponibles();
        } else {
            result = mascotaService.findAll();
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MascotaDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(mascotaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody MascotaRequestDTO request) {

        Integer idCentro = obtenerIdCentroDesdeToken(authHeader);
        if (idCentro != null) {
            request.setIdCentro(idCentro);
        }

        MascotaDTO created = mascotaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MascotaDTO> update(
            @PathVariable Integer id,
            @RequestBody MascotaRequestDTO request) {
        return ResponseEntity.ok(mascotaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        mascotaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Integer obtenerIdCentroDesdeToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return JwtUtil.obtenerIdCentro(token);
        }
        return null;
    }
}
