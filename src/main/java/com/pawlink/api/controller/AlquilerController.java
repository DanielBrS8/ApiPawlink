package com.pawlink.api.controller;

import com.pawlink.api.dto.AlquilerDTO;
import com.pawlink.api.dto.AlquilerRequestDTO;
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

    /**
     * GET /api/alquileres
     * Devuelve todos los alquileres.
     * Filtros opcionales: ?estado=activo | ?mascota=1 | ?voluntario=3
     */
    @GetMapping
    public ResponseEntity<List<AlquilerDTO>> getAll(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer mascota,
            @RequestParam(required = false) Integer voluntario) {

        List<AlquilerDTO> result;

        if (estado != null) {
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

    /**
     * GET /api/alquileres/{id}
     * Devuelve un alquiler por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlquilerDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(alquilerService.findById(id));
    }

    /**
     * POST /api/alquileres
     * Crea un nuevo alquiler.
     */
    @PostMapping
    public ResponseEntity<AlquilerDTO> create(@RequestBody AlquilerRequestDTO request) {
        AlquilerDTO created = alquilerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/alquileres/{id}
     * Actualiza un alquiler existente (p.ej. cambiar estado a "finalizado").
     */
    @PutMapping("/{id}")
    public ResponseEntity<AlquilerDTO> update(
            @PathVariable Integer id,
            @RequestBody AlquilerRequestDTO request) {
        return ResponseEntity.ok(alquilerService.update(id, request));
    }

    /**
     * DELETE /api/alquileres/{id}
     * Elimina un alquiler. Devuelve 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        alquilerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
