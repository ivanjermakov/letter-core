package com.github.ivanjermakov.lettercore.conversation.dto;

import java.util.List;

public class NewChatDto {

	public String name;
	public List<Long> userIds;

	public NewChatDto() {
	}

	public NewChatDto(String name, List<Long> userIds) {
		this.name = name;
		this.userIds = userIds;
	}

}
