package com.pawlink.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HiloWebDTO {

    private Long idHilo;
    private String titulo;
    private String autor;
    private Integer respuestas;
    private String fechaFormateada;
}
