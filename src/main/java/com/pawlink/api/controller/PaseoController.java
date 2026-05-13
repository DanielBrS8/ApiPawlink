package com.pawlink.api.controller;

import com.pawlink.api.dto.PaseoDTO;
import com.pawlink.api.dto.PaseoRequestDTO;
import com.pawlink.api.service.PaseoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paseos")
@RequiredArgsConstructor
public class PaseoController {

    private final PaseoService paseoService;

    @GetMapping
    public ResponseEntity<List<PaseoDTO>> getAll(@RequestParam(required = false) Integer mascotaId) {
        List<PaseoDTO> result = (mascotaId != null)
                ? paseoService.findByMascota(mascotaId)
                : paseoService.findAll();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<PaseoDTO> create(@RequestBody PaseoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paseoService.create(request));
    }
}
