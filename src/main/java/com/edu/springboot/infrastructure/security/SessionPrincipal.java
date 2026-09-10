package com.edu.springboot.infrastructure.security;

public record SessionPrincipal(String loginId, boolean admin) {
}
