package com.edu.springboot.domain.sms;

public interface SmsSender {

	void send(String to, String text);
}
