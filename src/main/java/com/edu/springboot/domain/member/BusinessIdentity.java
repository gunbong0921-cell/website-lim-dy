package com.edu.springboot.domain.member;

public record BusinessIdentity(
	String businessNumber,
	String openingDate,
	String representativeName,
	String companyName,
	String address
) {
	public boolean canValidate() {
		return notBlank(businessNumber) && notBlank(openingDate) && notBlank(representativeName);
	}

	private boolean notBlank(String value) {
		return value != null && !value.isBlank();
	}
}
