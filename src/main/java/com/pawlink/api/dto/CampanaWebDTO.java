package com.pawlink.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampanaWebDTO {

    private Long idCampana;
    private String titulo;
    private BigDecimal objetivoDinero;
    private BigDecimal recaudado;
    private String fotoUrl;
}
