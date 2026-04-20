package com.pawlink.api.controller;

import com.pawlink.api.dto.TamagotchiDTO;
import com.pawlink.api.service.TamagotchiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mascotas/{idMascota}/tamagotchi")
@RequiredArgsConstructor
public class TamagotchiController {

    private final TamagotchiService tamagotchiService;

    @GetMapping
    public ResponseEntity<TamagotchiDTO> obtener(@PathVariable Integer idMascota) {
        return ResponseEntity.ok(tamagotchiService.obtenerOCrear(idMascota));
    }

    @PutMapping
    public ResponseEntity<TamagotchiDTO> actualizar(
            @PathVariable Integer idMascota,
            @RequestBody TamagotchiDTO request) {
        return ResponseEntity.ok(tamagotchiService.actualizar(idMascota, request));
    }
}
