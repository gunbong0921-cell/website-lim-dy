package com.edu.springboot.infrastructure.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import com.edu.springboot.application.member.dto.MemberResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberSessionBinder {

	private final SecurityContextRepository securityContextRepository;

	public void bind(HttpServletRequest request, HttpServletResponse response, MemberResponse member) {
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		if (member.admin()) {
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		}
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			member.loginId(),
			null,
			authorities
		);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		securityContextRepository.saveContext(context, request, response);
	}
}
