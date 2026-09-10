package com.edu.springboot.infrastructure.nts;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.domain.member.BusinessIdentity;
import com.edu.springboot.domain.member.BusinessRegistrationGateway;
import com.edu.springboot.domain.member.BusinessRegistrationResult;

/**
 * Hexaq
 * 계층: Infrastructure
 * 객체: NtsOpendataBusinessRegistrationGateway
 * 책임: 외부 시스템 포트 또는 구현
 * 문서: [docs/features/02-business-registration.md](../../../../../../../../docs/features/02-business-registration.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Component
public class NtsOpendataBusinessRegistrationGateway implements BusinessRegistrationGateway {

	private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {
	};

	private final RestClient restClient;
	private final String baseUrl;
	private final String serviceKey;

	public NtsOpendataBusinessRegistrationGateway(
		@Value("${app.nts.base-url:https://api.odcloud.kr/api/nts-businessman/v1}") String baseUrl,
		@Value("${app.nts.service-key:}") String serviceKey
	) {
		this.restClient = RestClient.builder().build();
		this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
		this.serviceKey = serviceKey;
	}

	@Override
	public BusinessRegistrationResult lookupStatus(String businessNumber) {
		Map<String, Object> root = post("/status", Map.of("b_no", List.of(businessNumber)));
		Map<String, Object> item = firstData(root);
		if (item == null) {
			return unmatched("국세청에 등록되지 않은 사업자등록번호입니다.");
		}
		return fromStatus(item);
	}

	@Override
	public BusinessRegistrationResult validate(BusinessIdentity identity) {
		Map<String, Object> body = Map.of(
			"businesses",
			List.of(Map.of(
				"b_no", identity.businessNumber(),
				"start_dt", identity.openingDate(),
				"p_nm", identity.representativeName(),
				"p_nm2", "",
				"b_nm", nullToEmpty(identity.companyName()),
				"corp_no", "",
				"b_sector", "",
				"b_type", "",
				"b_adr", nullToEmpty(identity.address())
			))
		);
		Map<String, Object> root = post("/validate", body);
		Map<String, Object> item = firstData(root);
		if (item == null) {
			return unmatched("국세청에서 확인할 수 없습니다.");
		}
		if (!"01".equals(text(item, "valid"))) {
			String message = text(item, "valid_msg");
			return unmatched(message.isBlank() ? "입력한 사업자 정보가 일치하지 않습니다." : message);
		}
		Object status = item.get("status");
		if (status instanceof Map<?, ?> statusMap) {
			@SuppressWarnings("unchecked")
			Map<String, Object> typed = (Map<String, Object>) statusMap;
			return fromStatus(typed);
		}
		return new BusinessRegistrationResult(true, true, "확인됨", "", "사업자 정보가 일치합니다.");
	}

	private BusinessRegistrationResult fromStatus(Map<String, Object> item) {
		String taxType = text(item, "tax_type");
		if (taxType.contains("등록되지 않은")) {
			return unmatched(taxType);
		}
		String statusCode = text(item, "b_stt_cd");
		String statusName = text(item, "b_stt");
		boolean operating = "01".equals(statusCode);
		return new BusinessRegistrationResult(true, operating, statusName, taxType, join(statusName, taxType));
	}

	private Map<String, Object> post(String path, Object body) {
		if (serviceKey == null || serviceKey.isBlank()) {
			throw new BusinessException("국세청 API 키가 설정되지 않았습니다.");
		}
		try {
			Map<String, Object> json = restClient.post()
				.uri(URI.create(baseUrl + path + "?serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8)
					+ "&returnType=JSON"))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(body)
				.retrieve()
				.body(MAP_TYPE);
			return json == null ? Map.of() : json;
		} catch (RestClientResponseException ex) {
			throw new BusinessException("국세청 사업자 조회에 실패했습니다.");
		} catch (BusinessException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new BusinessException("국세청 사업자 조회에 실패했습니다.");
		}
	}

	private Map<String, Object> firstData(Map<String, Object> root) {
		Object data = root.get("data");
		if (!(data instanceof List<?> list) || list.isEmpty()) {
			return null;
		}
		Object first = list.get(0);
		if (first instanceof Map<?, ?> map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> typed = (Map<String, Object>) map;
			return typed;
		}
		return null;
	}

	private BusinessRegistrationResult unmatched(String message) {
		return new BusinessRegistrationResult(false, false, "", "", message);
	}

	private String text(Map<String, Object> node, String field) {
		Object value = node.get(field);
		return value == null ? "" : String.valueOf(value);
	}

	private String join(String left, String right) {
		if (left.isBlank()) {
			return right;
		}
		if (right.isBlank()) {
			return left;
		}
		return left + " · " + right;
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}
