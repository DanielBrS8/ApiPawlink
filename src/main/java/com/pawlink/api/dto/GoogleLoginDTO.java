package com.pawlink.api.dto;

import lombok.Data;

@Data
public class GoogleLoginDTO {
    private String idToken;
    private String code;
}
