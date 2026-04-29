package com.pawlink.api.controller;

import com.pawlink.api.dto.VacunaDTO;
import com.pawlink.api.service.VacunaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VacunaController {

    private final VacunaService vacunaService;

    @GetMapping("/api/mascotas/{id}/vacunas")
    public ResponseEntity<List<VacunaDTO>> getVacunasPorMascota(@PathVariable Integer id) {
        return ResponseEntity.ok(vacunaService.obtenerVacunasPorMascota(id));
    }

    @PostMapping("/api/vacunas")
    public ResponseEntity<VacunaDTO> registrarVacuna(@RequestBody VacunaDTO vacunaDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vacunaService.registrarVacuna(vacunaDto));
    }
}
