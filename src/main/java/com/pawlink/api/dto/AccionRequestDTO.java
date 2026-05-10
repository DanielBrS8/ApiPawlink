package com.pawlink.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccionRequestDTO {

    @JsonProperty("id_mascota_virtual")
    private Integer idMascotaVirtual;

    private String tipo;
}
