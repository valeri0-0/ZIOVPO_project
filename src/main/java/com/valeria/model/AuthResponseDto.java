package com.valeria.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDto
{
    private String accessToken;
    private String refreshToken;
}
