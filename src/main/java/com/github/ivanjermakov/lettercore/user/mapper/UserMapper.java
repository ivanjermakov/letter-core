package com.github.ivanjermakov.lettercore.user.mapper;

import com.github.ivanjermakov.lettercore.common.mapper.Mapper;
import com.github.ivanjermakov.lettercore.file.dto.AvatarDto;
import com.github.ivanjermakov.lettercore.file.mapper.AvatarMapper;
import com.github.ivanjermakov.lettercore.file.service.AvatarService;
import com.github.ivanjermakov.lettercore.user.dto.UserDto;
import com.github.ivanjermakov.lettercore.user.entity.User;
import com.github.ivanjermakov.lettercore.user.entity.UserOnline;
import com.github.ivanjermakov.lettercore.user.repository.UserOnlineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserMapper implements Mapper<User, UserDto> {

	@Value("${default.avatar.conversation.path}")
	private String defaultAvatarConversationPath;

	private UserOnlineRepository userOnlineRepository;
	private AvatarService avatarService;
	private AvatarMapper avatarMapper;

	@Autowired
	public void setUserOnlineRepository(UserOnlineRepository userOnlineRepository) {
		this.userOnlineRepository = userOnlineRepository;
	}

	@Autowired
	public void setAvatarService(AvatarService avatarService) {
		this.avatarService = avatarService;
	}

	@Autowired
	public void setAvatarMapper(AvatarMapper avatarMapper) {
		this.avatarMapper = avatarMapper;
	}

	@Override
	public UserDto map(User user) {
		UserOnline userOnline = userOnlineRepository.findFirstByUserIdOrderBySeenDesc(user.id);
		return new UserDto(
				user.id,
				user.login,
				user.userInfo.firstName,
				user.userInfo.lastName,
				avatarService.getCurrent(user).map(a -> avatarMapper.map(a))
						.orElse(new AvatarDto(null, defaultAvatarConversationPath, null)),
				Optional.ofNullable(userOnline).map(uo -> uo.seen).orElse(null)
		);
	}

}
