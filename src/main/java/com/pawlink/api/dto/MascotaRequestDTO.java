package com.pawlink.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de entrada para crear o actualizar una Mascota.
 */
@Data
public class MascotaRequestDTO {

    private Integer idCentro;
    private String nombre;
    private String especie;
    private String raza;
    private LocalDate fechaNacimiento;
    private BigDecimal peso;
    private String estadoSalud;
    private Integer disponibleAlquiler;
    private String foto;
    private String notas;
}
