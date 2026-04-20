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
public class MascotaVirtualDTO {

    private Integer idVirtual;
    private Integer idUsuario;
    private Integer idMascotaReal;
    private String nombre;
    private String especie;
    private Integer nivel;
    private Integer experiencia;
    private Integer hambre;
    private Integer felicidad;
    private Integer energia;
    private Integer higiene;
    private LocalDateTime ultimaInteraccion;
}
