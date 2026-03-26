package com.valeria.license.dto;

import lombok.Data;

@Data
public class ActivateLicenseRequest {

    private String activationKey;

    private String deviceMac;

    private String deviceName;
}