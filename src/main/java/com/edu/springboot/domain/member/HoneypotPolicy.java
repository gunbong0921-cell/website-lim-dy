package com.edu.springboot.domain.member;

public class HoneypotPolicy {

	public boolean tripped(String website) {
		return website != null && !website.isBlank();
	}
}
