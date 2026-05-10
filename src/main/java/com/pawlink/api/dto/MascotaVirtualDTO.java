package com.pawlink.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("id_virtual")
    private Integer idVirtual;

    @JsonProperty("id_usuario")
    private Integer idUsuario;

    @JsonProperty("id_mascota_real")
    private Integer idMascotaReal;

    private String nombre;
    private String especie;
    private Integer nivel;
    private Integer experiencia;
    private Integer hambre;
    private Integer felicidad;
    private Integer energia;
    private Integer higiene;

    @JsonProperty("ultima_interaccion")
    private LocalDateTime ultimaInteraccion;
}
