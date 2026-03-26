package com.valeria.license.dto;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateLicenseRequest {

    private UUID productId;

    private UUID typeId;

    private UUID ownerId;

    private Integer deviceCount;

    private String description;
}