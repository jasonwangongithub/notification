package com.jasonmwang.notification.repository;

import com.jasonmwang.notification.model.email.Email;

public interface IEmailPersistsRepository {
	public Email save(Email email);
}
