package com.pawlink.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaseoDTO {
    private Integer id;
    private Integer mascotaId;
    private LocalDateTime fecha;
    private String duracion;
    private BigDecimal distancia;
    private List<Coordenada> ruta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Coordenada {
        private Double latitude;
        private Double longitude;
    }
}
