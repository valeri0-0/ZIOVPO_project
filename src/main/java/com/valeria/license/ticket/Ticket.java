package com.valeria.license.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// Объект лицензии, передаваемый клиенту для проверки валидности
@Data
@AllArgsConstructor

public class Ticket {

    private Instant serverTime;

    private long ticketLifetime;

    private LocalDate activationDate;

    private LocalDate expirationDate;

    private UUID userId;

    private UUID deviceId;

    private Boolean blocked;
}