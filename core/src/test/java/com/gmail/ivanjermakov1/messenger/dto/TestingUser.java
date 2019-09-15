package com.gmail.ivanjermakov1.messenger.dto;

import com.gmail.ivanjermakov1.messenger.entity.User;

public class TestingUser {

	public User user;
	public UserDto userDto;
	public String token;

	public TestingUser() {
	}

	public TestingUser(User user, UserDto userDto, String token) {
		this.user = user;
		this.userDto = userDto;
		this.token = token;
	}

}