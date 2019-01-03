package com.gmail.ivanjermakov1.messenger.messaging.dto;

import java.util.List;

public class EditMessageDTO {
	
	private Long id;
	private String text;
	private List<MessageDTO> forwarded;
	
	public EditMessageDTO() {
	}
	
	public EditMessageDTO(Long id, String text, List<MessageDTO> forwarded) {
		this.id = id;
		this.text = text;
		this.forwarded = forwarded;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public List<MessageDTO> getForwarded() {
		return forwarded;
	}
	
	public void setForwarded(List<MessageDTO> forwarded) {
		this.forwarded = forwarded;
	}
	
}