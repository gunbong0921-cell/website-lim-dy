package com.edu.springboot.application.member;

import org.springframework.stereotype.Service;

import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.application.member.dto.BusinessVerifyCommand;
import com.edu.springboot.application.member.dto.BusinessVerifyResponse;
import com.edu.springboot.domain.member.BusinessIdentity;
import com.edu.springboot.domain.member.BusinessRegistrationGateway;
import com.edu.springboot.domain.member.BusinessRegistrationResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerifyBusinessRegistrationService {

	private final BusinessRegistrationGateway businessRegistrationGateway;

	public BusinessVerifyResponse verify(BusinessVerifyCommand command) {
		String businessNumber = digits(command.businessNumber());
		if (businessNumber.length() != 10) {
			throw new BusinessException("사업자등록번호 10자리를 입력하세요.");
		}
		String openingDate = digits(command.openingDate());
		if (openingDate.length() != 8) {
			throw new BusinessException("진위확인을 위해 개업일자를 입력하세요.");
		}
		BusinessIdentity identity = new BusinessIdentity(
			businessNumber,
			openingDate,
			trim(command.representativeName()),
			trim(command.companyName()),
			trim(command.address())
		);
		if (!identity.canValidate()) {
			throw new BusinessException("진위확인을 위해 대표자명과 개업일자를 입력하세요.");
		}
		BusinessRegistrationResult result = businessRegistrationGateway.validate(identity);
		if (!result.matched()) {
			throw new BusinessException(result.message() == null || result.message().isBlank()
				? "국세청에서 확인할 수 없는 사업자 정보입니다."
				: result.message());
		}
		if (!result.operating()) {
			throw new BusinessException(result.statusName() == null || result.statusName().isBlank()
				? "휴업 또는 폐업 사업자는 가입할 수 없습니다."
				: result.statusName() + " 상태의 사업자는 가입할 수 없습니다.");
		}
		return new BusinessVerifyResponse(true, true, result.statusName(), result.taxType());
	}

	private String digits(String value) {
		return value == null ? "" : value.replaceAll("\\D", "");
	}

	private String trim(String value) {
		return value == null ? "" : value.trim();
	}
}
