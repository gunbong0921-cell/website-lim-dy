package com.edu.springboot.infrastructure.mail;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.edu.springboot.domain.mail.MailSender;

import jakarta.mail.internet.MimeMessage;

@Component
public class SpringMailSender implements MailSender {

	private final JavaMailSender javaMailSender;
	private final String from;

	public SpringMailSender(ObjectProvider<JavaMailSender> javaMailSender,
		@Value("${spring.mail.username:}") String from) {
		this.javaMailSender = javaMailSender.getIfAvailable();
		this.from = from;
	}

	@Override
	public void send(String to, String subject, String body) {
		if (javaMailSender == null || from == null || from.isBlank()) {
			throw new IllegalStateException("메일 계정이 설정되지 않았습니다.");
		}
		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(wrapHtml(body), true);
			javaMailSender.send(message);
		} catch (Exception ex) {
			throw new IllegalStateException("메일 발송에 실패했습니다.", ex);
		}
	}

	private String wrapHtml(String body) {
		String contents = body == null ? "" : body.replace("\r\n", "<br>").replace("\n", "<br>");
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(new ClassPathResource("mail/MailTemplate.html").getInputStream(),
				StandardCharsets.UTF_8))) {
			String template = reader.lines().collect(Collectors.joining("\n"));
			return template.replace("__CONTENT__", contents);
		} catch (Exception ex) {
			return "<p>" + contents + "</p>";
		}
	}
}
