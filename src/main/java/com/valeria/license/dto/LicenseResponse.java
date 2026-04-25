package com.valeria.license.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LicenseResponse {

    private UUID id;
    private String code;
    private UUID productId;
    private UUID typeId;
    private Integer deviceCount;
    private Boolean blocked;
}