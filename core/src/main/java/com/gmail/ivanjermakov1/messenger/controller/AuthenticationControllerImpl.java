package com.gmail.ivanjermakov1.messenger.controller;

import com.gmail.ivanjermakov1.messenger.dto.UserDto;
import com.gmail.ivanjermakov1.messenger.entity.User;
import com.gmail.ivanjermakov1.messenger.exception.AuthenticationException;
import com.gmail.ivanjermakov1.messenger.mapper.UserMapper;
import com.gmail.ivanjermakov1.messenger.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
@Transactional
public class AuthenticationControllerImpl implements AuthenticationController {

	private final UserService userService;

	private final UserMapper userMapper;

	@Autowired
	public AuthenticationControllerImpl(UserService userService, UserMapper userMapper) {
		this.userService = userService;
		this.userMapper = userMapper;
	}

	@Override
	@GetMapping
	public String authenticate(@RequestParam("login") String login, @RequestParam("password") String password) throws AuthenticationException {
		return userService.authenticate(login, password);
	}

	@Override
	@GetMapping("validate")
	public UserDto validate(@ModelAttribute User user,
	                        @RequestHeader("Auth-Token") String token) throws AuthenticationException {
		userService.authenticate(token);
		userService.appearOnline(user);
		return userMapper.map(user);
	}

	@Override
	@GetMapping("logout")
	public void logout(@ModelAttribute User user) {
		userService.logout(user);
	}

}