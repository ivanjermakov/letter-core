package com.github.ivanjermakov.lettercore.conversation.controller;

import com.github.ivanjermakov.lettercore.conversation.dto.PreviewDto;
import com.github.ivanjermakov.lettercore.conversation.entity.Conversation;
import com.github.ivanjermakov.lettercore.conversation.mapper.PreviewMapper;
import com.github.ivanjermakov.lettercore.conversation.service.ConversationService;
import com.github.ivanjermakov.lettercore.conversation.service.PreviewService;
import com.github.ivanjermakov.lettercore.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("preview")
@Transactional
public class PreviewController {

	private final PreviewService previewService;
	private final ConversationService conversationService;

	private final PreviewMapper previewMapper;

	@Autowired
	public PreviewController(PreviewService previewService, ConversationService conversationService, PreviewMapper previewMapper) {
		this.previewService = previewService;
		this.conversationService = conversationService;
		this.previewMapper = previewMapper;
	}

	/**
	 * List previews.
	 *
	 * @param user authenticated user. automatically maps, when {@literal Auth-Token} parameter present
	 * @return list of previews
	 */
	@GetMapping("all")
	public List<PreviewDto> all(@ModelAttribute User user,
	                            Pageable pageable) {
		return previewService.all(user, pageable);
	}

	/**
	 * Get specified conversation.
	 *
	 * @param user           authenticated user. automatically maps, when {@literal Auth-Token} parameter present
	 * @param conversationId conversation id
	 * @return conversation
	 */
	@GetMapping("get")
	public PreviewDto get(@ModelAttribute User user,
	                      @RequestParam("conversationId") Long conversationId) {
		Conversation conversation = conversationService.get(conversationId);

		PreviewDto preview = previewMapper.with(user).map(conversation);
		conversationService.show(user, conversation);

		return preview;
	}

}
