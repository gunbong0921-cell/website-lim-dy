package com.edu.springboot.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Hexaq
 * 계층: Presentation
 * 객체: SpaController
 * 책임: HTTP 입구. 검증·Application 호출·ApiResponse. Domain 객체 비노출
 * 문서: [docs/technical/02-technical-specification.md](../../../../../../../../docs/technical/02-technical-specification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Controller
public class SpaController {

	@GetMapping({
		"/",
		"/solutions",
		"/insights",
		"/login",
		"/signup",
		"/mypage",
		"/forgot-password",
		"/forgot-id",
		"/board/free",
		"/board/free/**",
		"/board/qna",
		"/board/qna/**",
		"/board/archive",
		"/board/archive/**"
	})
	public String spa() {
		return "forward:/index.html";
	}
}
