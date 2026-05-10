package com.pawlink.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccionResponseDTO {

    @JsonProperty("id_accion")
    private Integer idAccion;

    @JsonProperty("puntos_ganados")
    private Integer puntosGanados;

    private MascotaVirtualDTO mascota;
}
