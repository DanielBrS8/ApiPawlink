package com.pawlink.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CentroWebDTO {

    private Integer idCentro;
    private String nombre;
    private String ciudad;
    private String especialidad;
    private String fotoUrl;
}
