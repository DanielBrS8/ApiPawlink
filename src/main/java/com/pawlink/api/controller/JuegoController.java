package com.pawlink.api.controller;

import com.pawlink.api.dto.MascotaVirtualDTO;
import com.pawlink.api.service.JuegoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/juego")
@RequiredArgsConstructor
public class JuegoController {

    private final JuegoService juegoService;

    @GetMapping("/mascota/{idMascota}")
    public ResponseEntity<MascotaVirtualDTO> obtener(@PathVariable Integer idMascota) {
        return ResponseEntity.ok(juegoService.obtenerOCrear(idMascota));
    }

    @PutMapping("/mascota/{idMascota}")
    public ResponseEntity<MascotaVirtualDTO> actualizar(
            @PathVariable Integer idMascota,
            @RequestBody MascotaVirtualDTO request) {
        return ResponseEntity.ok(juegoService.actualizar(idMascota, request));
    }
}
