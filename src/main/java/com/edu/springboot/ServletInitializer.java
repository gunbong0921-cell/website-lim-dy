package com.edu.springboot;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Hexaq
 * 계층: Bootstrap
 * 객체: ServletInitializer
 * 책임: WAR 배포 시 SpringBootServletInitializer
 * 문서: [docs/technical/02-technical-specification.md](../../../../../../docs/technical/02-technical-specification.md)
 */
public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(WebsiteLimDyApplication.class);
	}

}
