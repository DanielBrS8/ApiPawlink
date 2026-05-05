package com.pawlink.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MascotaWebDTO {

    private Long idMascota;
    private String nombre;
    private String especie;
    private String raza;
    private String edadAprox;
    private String fotoUrl;
}
