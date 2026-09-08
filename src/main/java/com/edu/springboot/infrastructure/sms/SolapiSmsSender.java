package com.edu.springboot.infrastructure.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.edu.springboot.domain.sms.SmsSender;
import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;

@Component
public class SolapiSmsSender implements SmsSender {

	private final DefaultMessageService messageService;
	private final String from;

	public SolapiSmsSender(
		@Value("${app.solapi.api-key:}") String apiKey,
		@Value("${app.solapi.api-secret:}") String apiSecret,
		@Value("${app.solapi.from:}") String from
	) {
		this.from = from == null ? "" : from.replaceAll("\\D", "");
		if (apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
			this.messageService = null;
		} else {
			this.messageService = SolapiClient.INSTANCE.createInstance(apiKey.trim(), apiSecret.trim());
		}
	}

	@Override
	public void send(String to, String text) {
		if (messageService == null) {
			throw new IllegalStateException("솔라피 API 키가 설정되지 않았습니다.");
		}
		if (from.isBlank()) {
			throw new IllegalStateException("솔라피 발신번호가 설정되지 않았습니다. app.solapi.from 에 등록된 번호를 넣어 주세요.");
		}
		try {
			Message message = new Message();
			message.setFrom(from);
			message.setTo(to);
			message.setText(text);
			messageService.send(message, null);
		} catch (SolapiMessageNotReceivedException ex) {
			throw new IllegalStateException("문자 발송에 실패했습니다. 발신번호와 잔액을 확인해 주세요.", ex);
		} catch (Exception ex) {
			throw new IllegalStateException("문자 발송에 실패했습니다.", ex);
		}
	}
}
