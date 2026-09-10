package com.edu.springboot.infrastructure.security;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: CachedBodyHttpServletRequest
 * 책임: CachedBodyHttpServletRequest 역할. 클린 아키텍처 계층을 지키며 문서의 객체를 구현
 * 문서: [docs/security/overview.md](../../../../../../../../docs/security/overview.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

	private final byte[] body;

	public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
		super(request);
		this.body = request.getInputStream().readAllBytes();
	}

	public byte[] cachedBody() {
		return body;
	}

	@Override
	public ServletInputStream getInputStream() {
		ByteArrayInputStream input = new ByteArrayInputStream(body);
		return new ServletInputStream() {
			@Override
			public int read() {
				return input.read();
			}

			@Override
			public boolean isFinished() {
				return input.available() == 0;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setReadListener(ReadListener listener) {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public BufferedReader getReader() {
		String encoding = getCharacterEncoding();
		Charset charset = encoding == null || encoding.isBlank()
			? StandardCharsets.UTF_8
			: Charset.forName(encoding);
		return new BufferedReader(new InputStreamReader(getInputStream(), charset));
	}
}
