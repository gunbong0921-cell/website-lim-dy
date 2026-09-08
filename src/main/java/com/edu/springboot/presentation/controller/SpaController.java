package com.edu.springboot.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
