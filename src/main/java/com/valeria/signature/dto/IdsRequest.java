package com.valeria.signature.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class IdsRequest {

    private List<UUID> ids;

}