package com.edu.springboot.domain.member;

public interface BusinessRegistrationGateway {

	BusinessRegistrationResult lookupStatus(String businessNumber);

	BusinessRegistrationResult validate(BusinessIdentity identity);
}
