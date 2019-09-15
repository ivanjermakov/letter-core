package com.gmail.ivanjermakov1.messenger.controller;

import com.gmail.ivanjermakov1.messenger.dto.EditMessageDto;
import com.gmail.ivanjermakov1.messenger.dto.MessageDto;
import com.gmail.ivanjermakov1.messenger.dto.NewMessageDto;
import com.gmail.ivanjermakov1.messenger.dto.action.Action;
import com.gmail.ivanjermakov1.messenger.dto.action.Request;
import com.gmail.ivanjermakov1.messenger.entity.User;
import com.gmail.ivanjermakov1.messenger.exception.AuthenticationException;
import com.gmail.ivanjermakov1.messenger.exception.AuthorizationException;
import com.gmail.ivanjermakov1.messenger.exception.InvalidMessageException;
import com.gmail.ivanjermakov1.messenger.service.MessagingService;
import com.gmail.ivanjermakov1.messenger.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

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

	/**
	 * Subscribe on server-sent events.
	 * Calling user will receive events invoked by his own actions.
	 *
	 * @param token user token
	 * @return event stream of {@link Action}s
	 * @throws AuthenticationException on invalid @param token
	 */
	@GetMapping("listen")
	public Flux<Action> getEvents(@RequestParam("token") String token) throws AuthenticationException {
		User user = userService.authenticate(token);

		return Flux.create(sink -> messagingService.connect(new Request<>(user, sink)));
	}

	/**
	 * Send message and invoke {@code com.gmail.ivanjermakov1.messenger.dto.action.MessageEditAction}.
	 *
	 * @param user          authenticated user. automatically maps, when {@literal Auth-Token} parameter present
	 * @param newMessageDto new message
	 * @return sent message
	 * @throws AuthenticationException on invalid @param token
	 * @throws InvalidMessageException on invalid @param newMessageDto
	 */
	@PostMapping("send")
	@Transactional
	public MessageDto sendMessage(@ModelAttribute User user,
	                              @RequestBody NewMessageDto newMessageDto) throws AuthenticationException, InvalidMessageException, AuthorizationException {
		messagingService.processConversationRead(user, newMessageDto.conversationId);
		return messagingService.processNewMessage(user, newMessageDto);
	}

	/**
	 * Edit message.
	 *
	 * @param user           authenticated user. automatically maps, when {@literal Auth-Token} parameter present
	 * @param editMessageDto editing message
	 * @return edited message
	 * @throws AuthenticationException on invalid @param token
	 */
	@PostMapping("edit")
	@Transactional
	public MessageDto editMessage(@ModelAttribute User user,
	                              @RequestBody EditMessageDto editMessageDto) throws AuthenticationException {
		return messagingService.processMessageEdit(user, editMessageDto);
	}

}