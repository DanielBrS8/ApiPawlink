package com.pawlink.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResenaCreateDTO {

    private Integer puntuacion;
    private String comentario;
}
