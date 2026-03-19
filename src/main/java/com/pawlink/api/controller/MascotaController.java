package com.pawlink.api.controller;

import com.pawlink.api.dto.MascotaDTO;
import com.pawlink.api.dto.MascotaRequestDTO;
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

    /**
     * GET /api/mascotas
     * Devuelve todas las mascotas.
     * Acepta query param ?disponible=1 para filtrar las disponibles para alquiler.
     */
    @GetMapping
    public ResponseEntity<List<MascotaDTO>> getAll(
            @RequestParam(required = false) Integer disponible) {

        List<MascotaDTO> result = (disponible != null)
                ? mascotaService.findDisponibles()
                : mascotaService.findAll();

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/mascotas/{id}
     * Devuelve una mascota por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MascotaDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(mascotaService.findById(id));
    }

    /**
     * POST /api/mascotas
     * Crea una nueva mascota.
     */
    @PostMapping
    public ResponseEntity<MascotaDTO> create(@RequestBody MascotaRequestDTO request) {
        MascotaDTO created = mascotaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/mascotas/{id}
     * Actualiza completamente una mascota existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MascotaDTO> update(
            @PathVariable Integer id,
            @RequestBody MascotaRequestDTO request) {
        return ResponseEntity.ok(mascotaService.update(id, request));
    }

    /**
     * DELETE /api/mascotas/{id}
     * Elimina una mascota. Devuelve 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        mascotaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
