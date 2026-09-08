package com.edu.springboot.domain.member;

public interface PasswordEncryptor {

	String encode(String raw);

	boolean matches(String raw, String encoded);
}
