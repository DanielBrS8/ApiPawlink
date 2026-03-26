package com.pawlink.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private Integer id;
    private String nombre;
    private String rol;
    private Integer idCentro;
}
