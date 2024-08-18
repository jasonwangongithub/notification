package com.jasonwangongithub.starter.email.operation;

import org.springframework.stereotype.Component;

import com.jasonmwang.notification.model.email.Email;
import com.jasonmwang.notification.repository.IEmailPersistsRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailOperation {

	private final IEmailPersistsRepository emailPersistsRepository;
	public String handle(Email email) {
		email = emailPersistsRepository.save(email);
		
		return email == null? "failed" : "success";
	}
}
