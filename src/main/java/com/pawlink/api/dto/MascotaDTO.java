package com.pawlink.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para Mascota.
 * Expone solo los campos necesarios hacia los clientes, evitando recursión Jackson.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MascotaDTO {

    private Integer idMascota;

    // Del centro asociado devolvemos solo id y nombre (sin traer todo el grafo)
    private Integer idCentro;
    private String nombreCentro;

    private String nombre;
    private String especie;
    private String raza;
    private LocalDate fechaNacimiento;
    private BigDecimal peso;
    private String estadoSalud;
    private Integer disponibleAlquiler;
    private String foto;
    private String notas;
    private LocalDateTime fechaCreacion;
}
