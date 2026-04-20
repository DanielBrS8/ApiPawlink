package com.pawlink.api.controller;

import com.pawlink.api.dto.HistorialAdopcionesDTO;
import com.pawlink.api.dto.UsuarioDTO;
import com.pawlink.api.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/{id}/historial-adopciones")
    public ResponseEntity<HistorialAdopcionesDTO> historialAdopciones(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.obtenerHistorialAdopciones(id));
    }

    @PutMapping("/{id}/vincular/{idCentro}")
    public ResponseEntity<UsuarioDTO> vincularCentro(
            @PathVariable Integer id,
            @PathVariable Integer idCentro) {
        return ResponseEntity.ok(usuarioService.vincularCentro(id, idCentro));
    }
}
