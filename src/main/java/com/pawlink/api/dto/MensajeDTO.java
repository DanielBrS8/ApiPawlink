package com.pawlink.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensajeDTO {

    private Integer idMensaje;
    private Integer idEmisor;
    private String contenido;
    private LocalDateTime fecha;
    private Boolean leido;
}
