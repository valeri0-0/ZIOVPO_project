package com.valeria.license.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.valeria.license.crypto.KeyProvider;
import com.valeria.license.ticket.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.security.Signature;
import java.util.Base64;


// Сервис создания криптографической подписи для ticket лицензии
@Component
@RequiredArgsConstructor
public class TicketSigner {

    private final KeyProvider keyProvider;

    public String signTicket(Ticket ticket) {

        try {

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            String ticketJson = mapper.writeValueAsString(ticket);

            Signature signature = Signature.getInstance("SHA256withRSA");

            signature.initSign(keyProvider.getPrivateKey());

            signature.update(ticketJson.getBytes());

            byte[] signed = signature.sign();

            return Base64.getEncoder().encodeToString(signed);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка подписи ticket", e);
        }
    }
}