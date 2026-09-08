package com.edu.springboot.domain.mail;

public interface MailSender {

	void send(String to, String subject, String body);
}
