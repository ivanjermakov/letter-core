package com.gmail.ivanjermakov1.messenger.messaging.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gmail.ivanjermakov1.messenger.auth.dto.UserDto;

import java.time.LocalDate;

public class DocumentDto {

	public Long id;
	public UserDto user;
	public String path;

	@JsonFormat(pattern = "yyyy-MM-dd")
	public LocalDate uploaded;

	public DocumentDto() {
	}

	public DocumentDto(Long id, UserDto user, String path, LocalDate uploaded) {
		this.id = id;
		this.user = user;
		this.path = path;
		this.uploaded = uploaded;
	}

}
