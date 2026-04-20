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
public class TamagotchiDTO {

    private Long id;
    private Integer idMascota;
    private double hambre;
    private double energia;
    private double felicidad;
    private double suciedad;
    private LocalDateTime ultimaInteraccion;
}
