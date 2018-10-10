package com.gmail.ivanjermakov1.messenger.messaging.controller;

import com.gmail.ivanjermakov1.messenger.auth.entity.User;
import com.gmail.ivanjermakov1.messenger.auth.service.UserService;
import com.gmail.ivanjermakov1.messenger.exception.AuthenticationException;
import com.gmail.ivanjermakov1.messenger.exception.NoSuchEntityException;
import com.gmail.ivanjermakov1.messenger.messaging.entity.FullMessage;
import com.gmail.ivanjermakov1.messenger.messaging.entity.Message;
import com.gmail.ivanjermakov1.messenger.messaging.service.MessagingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping("messaging")
public class MessagingController {
	
	private final MessagingService messagingService;
	private final UserService userService;
	
	@Autowired
	public MessagingController(MessagingService messagingService, UserService userService) {
		this.messagingService = messagingService;
		this.userService = userService;
	}
	
	@RequestMapping("get")
	@GetMapping
	public DeferredResult<FullMessage> getMessage(@RequestParam("token") String token) throws AuthenticationException {
		try {
			User user = userService.getUser(userService.getUserId(token));
			
			DeferredResult<FullMessage> request = new DeferredResult<>();
			request.onTimeout(() -> messagingService.removeRequest(request));
			messagingService.addRequest(user, request);
			
			return request;
		} catch (NoSuchEntityException e) {
			throw new AuthenticationException("invalid token");
		}
	}
	
	@RequestMapping("send")
	@PostMapping
	public void sendMessage(@RequestParam("token") String token, @RequestBody Message message) throws AuthenticationException {
		try {
			User user = userService.getUser(userService.getUserId(token));
			messagingService.process(user, message);
		} catch (NoSuchEntityException e) {
			throw new AuthenticationException("invalid token");
		}
	}
	
	
}
