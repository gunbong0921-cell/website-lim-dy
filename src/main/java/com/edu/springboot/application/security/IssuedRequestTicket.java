package com.edu.springboot.application.security;

import java.time.Instant;

public record IssuedRequestTicket(String ticketId, String signingKey, Instant expiresAt) {
}
