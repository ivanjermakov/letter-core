package com.github.ivanjermakov.lettercore.conversation.integration;

import com.github.ivanjermakov.lettercore.auth.exception.AuthenticationException;
import com.github.ivanjermakov.lettercore.auth.exception.AuthorizationException;
import com.github.ivanjermakov.lettercore.auth.exception.RegistrationException;
import com.github.ivanjermakov.lettercore.common.dto.TestingUser;
import com.github.ivanjermakov.lettercore.common.exception.NoSuchEntityException;
import com.github.ivanjermakov.lettercore.common.service.TestingService;
import com.github.ivanjermakov.lettercore.conversation.controller.ChatController;
import com.github.ivanjermakov.lettercore.conversation.controller.PreviewController;
import com.github.ivanjermakov.lettercore.conversation.dto.ConversationDto;
import com.github.ivanjermakov.lettercore.conversation.dto.NewChatDto;
import com.github.ivanjermakov.lettercore.messaging.controller.MessagingController;
import com.github.ivanjermakov.lettercore.messaging.dto.MessageDto;
import com.github.ivanjermakov.lettercore.messaging.dto.NewMessageDto;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
public class ChatTest {

	@Autowired
	private PreviewController previewController;

	@Autowired
	private ChatController chatController;

	@Autowired
	private MessagingController messagingController;

	@Autowired
	private TestingService testingService;

	private TestingUser user1;
	private TestingUser user2;
	private ConversationDto chat;
	private MessageDto message1;
	private MessageDto message2;

	@Before
	public void before() throws RegistrationException, AuthenticationException, AuthorizationException {
		user1 = testingService.registerUser("Jack");
		user2 = testingService.registerUser("Ron");

		chat = chatController.create(
				user1.user,
				new NewChatDto(
						"chat",
						new ArrayList<>(Collections.singletonList(user2.user.id))
				)
		);

		message1 = messagingController.sendMessage(
				user1.user,
				new NewMessageDto(
						user1.user.id,
						chat.id,
						"Hello!"
				)
		);

		message2 = messagingController.sendMessage(
				user2.user,
				new NewMessageDto(
						user2.user.id,
						chat.id,
						"Hello2!"
				)
		);
	}

	@Test
	public void shouldCreateChat() {
		Assert.assertNotNull(chat);
		Assert.assertEquals(2, chat.users.size());
	}

	@Test
	public void shouldAddMember() throws RegistrationException, AuthenticationException {
		TestingUser user3 = testingService.registerUser("John");

		chatController.addMember(
				user1.user,
				chat.id,
				user3.user.id
		);

		ConversationDto conversation = previewController.get(
				user1.user,
				chat.id
		).conversation;

		Assert.assertEquals(3, conversation.users.size());
	}

	@Test(expected = NoSuchEntityException.class)
	public void shouldThrowException_WhenNoSuchMemberAddingMember() throws AuthenticationException {
		chatController.addMember(
				user1.user,
				chat.id,
				-1L
		);
	}

	@Test(expected = NoSuchEntityException.class)
	public void shouldThrowException_WhenNoSuchChatAddingMember() throws AuthenticationException, RegistrationException {
		TestingUser user3 = testingService.registerUser("John");

		chatController.addMember(
				user1.user,
				-1L,
				user3.user.id
		);
	}

	@Test
	public void shouldAddMembers() throws RegistrationException, AuthenticationException {
		TestingUser user3 = testingService.registerUser("John");
		TestingUser user4 = testingService.registerUser("Bob");

		chatController.addMembers(
				user1.user,
				chat.id,
				Stream
						.of(user3, user4)
						.map(u -> u.user.id)
						.collect(Collectors.toList())
		);

		ConversationDto conversation = previewController.get(
				user1.user,
				chat.id
		).conversation;

		Assert.assertEquals(4, conversation.users.size());
	}

	@Test(expected = NoSuchEntityException.class)
	public void shouldThrowException_WhenNoSuchMemberAddingMembers() throws AuthenticationException {
		chatController.addMembers(
				user1.user,
				chat.id,
				new ArrayList<>(Collections.singletonList(-1L))
		);
	}

	@Test(expected = NoSuchEntityException.class)
	public void shouldThrowException_WhenNoSuchChatAddingMembers() throws AuthenticationException, RegistrationException {
		TestingUser user3 = testingService.registerUser("John");

		chatController.addMembers(
				user1.user,
				-1L,
				new ArrayList<>(Collections.singletonList(user3.user.id))
		);
	}

	@Test
	public void shouldKickMember() throws AuthenticationException {
		chatController.kickMember(
				user1.user,
				chat.id,
				user2.user.id
		);

		ConversationDto conversation = previewController.get(
				user1.user,
				chat.id
		).conversation;

		Assert.assertEquals(1, conversation.users.size());
	}

	@Test(expected = NoSuchEntityException.class)
	public void shouldThrowException_WhenNoSuchChatKickingMember() throws AuthenticationException, RegistrationException {
		TestingUser user3 = testingService.registerUser("John");

		chatController.kickMember(
				user1.user,
				-1L,
				user3.user.id
		);
	}

	@Test
	public void shouldHideChat() throws AuthenticationException {
		chatController.hide(
				user1.user,
				chat.id
		);

		ConversationDto conversationForUser1 = previewController.get(
				user1.user,
				chat.id
		).conversation;

		Assert.assertNotNull(conversationForUser1);
		Assert.assertTrue(conversationForUser1.hidden);

		ConversationDto conversationForUser2 = previewController.get(
				user2.user,
				chat.id
		).conversation;

		Assert.assertNotNull(conversationForUser2);
		Assert.assertFalse(conversationForUser2.hidden);

		Assert.assertEquals(conversationForUser1.id, conversationForUser2.id);
	}

	@Test
	public void shouldDeleteChat() throws AuthenticationException {
		chatController.delete(
				user1.user,
				chat.id
		);

		ConversationDto conversationForUser1 = previewController.get(
				user1.user,
				chat.id
		).conversation;

		Assert.assertNotNull(conversationForUser1);
		Assert.assertTrue(conversationForUser1.hidden);

		ConversationDto conversationForUser2 = previewController.get(
				user2.user,
				chat.id
		).conversation;

		Assert.assertNotNull(conversationForUser2);
		Assert.assertFalse(conversationForUser2.hidden);

		Assert.assertEquals(conversationForUser1.id, conversationForUser2.id);
	}

	@Ignore
	@Test
	public void shouldLeave() {
//      TODO
	}

}
