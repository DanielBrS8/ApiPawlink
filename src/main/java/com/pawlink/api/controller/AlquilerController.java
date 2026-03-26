package com.pawlink.api.controller;

import com.pawlink.api.dto.AlquilerDTO;
import com.pawlink.api.dto.AlquilerRequestDTO;
import com.pawlink.api.security.JwtUtil;
import com.pawlink.api.service.AlquilerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alquileres")
@RequiredArgsConstructor
public class AlquilerController {

    private final AlquilerService alquilerService;

    @GetMapping
    public ResponseEntity<List<AlquilerDTO>> getAll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer mascota,
            @RequestParam(required = false) Integer voluntario) {

        Integer idCentro = obtenerIdCentroDesdeToken(authHeader);

        List<AlquilerDTO> result;

        if (idCentro != null) {
            result = alquilerService.findByCentro(idCentro);
        } else if (estado != null) {
            result = alquilerService.findByEstado(estado);
        } else if (mascota != null) {
            result = alquilerService.findByMascota(mascota);
        } else if (voluntario != null) {
            result = alquilerService.findByVoluntario(voluntario);
        } else {
            result = alquilerService.findAll();
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlquilerDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(alquilerService.findById(id));
    }

    @PostMapping
    public ResponseEntity<AlquilerDTO> create(@RequestBody AlquilerRequestDTO request) {
        AlquilerDTO created = alquilerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlquilerDTO> update(
            @PathVariable Integer id,
            @RequestBody AlquilerRequestDTO request) {
        return ResponseEntity.ok(alquilerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        alquilerService.delete(id);
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
