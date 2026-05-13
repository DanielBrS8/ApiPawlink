package com.pawlink.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PaseoRequestDTO {
    private Integer mascotaId;
    private LocalDateTime fecha;
    private String duracion;
    private BigDecimal distancia;
    private List<PaseoDTO.Coordenada> ruta;
}
