package com.valeria.license.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CheckLicenseRequest {

    private String deviceMac;

    private UUID productId;

}