package com.coing.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MessageUtil {

	private final MessageSource messageSource;

	public MessageUtil(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public String resolveMessage(String code) {
		return messageSource.getMessage(code, null, code, LocaleContextHolder.getLocale());
	}
}
