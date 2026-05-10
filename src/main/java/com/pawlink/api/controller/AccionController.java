package com.pawlink.api.controller;

import com.pawlink.api.dto.AccionRequestDTO;
import com.pawlink.api.dto.AccionResponseDTO;
import com.pawlink.api.service.AccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/acciones")
@RequiredArgsConstructor
public class AccionController {

    private final AccionService accionService;

    @PostMapping
    public ResponseEntity<AccionResponseDTO> realizar(@RequestBody AccionRequestDTO request) {
        return ResponseEntity.ok(accionService.realizar(request));
    }
}
