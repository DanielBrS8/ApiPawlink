package com.pawlink.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacunaDTO {
    private Integer idVacuna;
    private Integer idMascota;
    private String nombreVacuna;
    private LocalDate fechaAdministracion;
    private LocalDate fechaProximaDosis;
    private String veterinario;
    private String notas;
}
