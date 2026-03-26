package com.pawlink.api.dto;

import lombok.Data;

@Data
public class CrearUsuarioDTO {
    private String nombre;
    private String email;
    private String password;
    private String rol;
    private Integer idCentro;
}
