package com.jasonwangongithub.starter.email;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.jasonmwang.notification.model.email.Email;
import com.jasonwangongithub.starter.email.operation.EmailOperation;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class EmailServiceApi {
	
	private final EmailOperation emailOperation;

	@PostMapping(value = "/deliver-email")
	public ResponseEntity<String> deliverEmail(@RequestBody Email email) {

		return ResponseEntity.ok(emailOperation.handle(email));
	}
}
