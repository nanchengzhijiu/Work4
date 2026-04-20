package work.work4.service.Interface;

import work.work4.common.RestBean;
import work.work4.dto.CreateGroupDto;

public interface ChatServiceInterface {
    RestBean<Object> createPrivate(String userId, String toUserId);
    RestBean<Object> createGroup(CreateGroupDto createGroupDto);
    RestBean<Object> getChatList(String userId);
}
