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
public class ConsultaChatDTO {

    private Integer idConversacion;
    private String asunto;
    private String estado;
    private LocalDateTime fechaInicio;

    private Integer idOtroParticipante;
    private String nombreOtroParticipante;

    private String ultimoMensaje;
    private LocalDateTime fechaUltimoMensaje;
}
