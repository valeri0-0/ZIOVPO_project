package com.valeria.license.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class RenewLicenseRequest {

    private String activationKey;

}