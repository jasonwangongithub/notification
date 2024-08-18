package com.jasonwangongithub.starter.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasonmwang.notification.model.email.Email;
import com.jasonmwang.notification.repository.IEmailPersistsRepository;
import com.jasonwangongithub.starter.email.operation.EmailOperation;

@SpringBootTest(classes = {EmailServiceApi.class, EmailOperation.class})
@Import(EmailServiceApiTest.TestConfig.class)
class EmailServiceApiTest {
	
	@Autowired
	private IEmailPersistsRepository emailPersistsRepository;
	
//	@Autowired
//	private TestRestTemplate restTemplate;
	
	@Autowired
	private EmailServiceApi emailServiceApi;
	
	@Test
	void testFailed() {
		ArgumentCaptor<Email> emailCap = ArgumentCaptor.forClass(Email.class);
		when(emailPersistsRepository.save(emailCap.capture())).thenReturn(null);
		
		Email email = new Email();
		email.setNotificationId("aaa-123");
//		ResponseEntity<String> result = restTemplate.postForEntity("http://localhost:8080/", email, String.class);
		
		ResponseEntity<String> result =  emailServiceApi.deliverEmail(email);
		
		assertEquals("failed", result.getBody());
	}

	@Test
	void testSuccess() {
		ObjectMapper objectMapper = new ObjectMapper();
		try(InputStream requestInput = new ClassPathResource("email_request.json").getInputStream()){
			Email email = objectMapper.readValue(requestInput, Email.class);
			ArgumentCaptor<Email> emailCap = ArgumentCaptor.forClass(Email.class);
			when(emailPersistsRepository.save(emailCap.capture())).thenReturn(email);
//			ResponseEntity<String> result = restTemplate.postForEntity("http://localhost:8080/", email, String.class);
			
			ResponseEntity<String> result =  emailServiceApi.deliverEmail(email);
			
			assertEquals("success", result.getBody());
			assertEquals("aa-123", emailCap.getValue().getNotificationId());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
    @Configuration
    static class TestConfig {
		
		@Bean
		IEmailPersistsRepository emailPersistsRepository() {
			return mock(IEmailPersistsRepository.class);
		}
	}

}
