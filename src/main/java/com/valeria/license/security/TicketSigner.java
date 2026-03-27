package com.valeria.license.security;

import com.valeria.license.ticket.Ticket;
import com.valeria.signature.SigningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Сервис подписи ticket через ЭЦП
@Component
@RequiredArgsConstructor
public class TicketSigner {

    private final SigningService signingService;

    public String signTicket(Ticket ticket) {

        // "подписываем ticket через SigningService"
        return signingService.sign(ticket);
    }
}