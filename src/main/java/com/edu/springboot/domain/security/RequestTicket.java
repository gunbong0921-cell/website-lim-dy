package com.edu.springboot.domain.security;

import java.time.Instant;

public record RequestTicket(String ticketId, String signingKey, Instant expiresAt) {
}
