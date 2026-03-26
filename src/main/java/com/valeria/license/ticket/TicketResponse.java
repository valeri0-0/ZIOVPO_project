package com.valeria.license.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;

// Ответ сервера с ticket лицензии и его цифровой подписью
@Data
@AllArgsConstructor
public class TicketResponse {

    private Ticket ticket;

    private String signature;
}