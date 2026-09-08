package com.edu.springboot;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	@GetMapping("/api/hello")
	public Map<String, String> hello() {
		return Map.of("message", "Spring Boot 연결 성공");
	}
}
