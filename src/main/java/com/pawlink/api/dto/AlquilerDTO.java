package com.pawlink.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO de respuesta para Alquiler.
 * Aplana la relación Mascota <-> Usuario para evitar recursión Jackson.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlquilerDTO {

    private Integer idAlquiler;

    // Datos de la mascota involucrada
    private Integer idMascota;
    private String nombreMascota;
    private String especieMascota;

    // Datos del voluntario
    private Integer idVoluntario;
    private String nombreVoluntario;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
}
