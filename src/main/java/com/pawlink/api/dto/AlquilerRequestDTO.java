package com.pawlink.api.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * DTO de entrada para crear o actualizar un Alquiler.
 */
@Data
public class AlquilerRequestDTO {

    private Integer idMascota;
    private Integer idVoluntario;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
}
