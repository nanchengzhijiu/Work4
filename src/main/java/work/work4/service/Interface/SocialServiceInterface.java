package work.work4.service.Interface;

import work.work4.dto.FollowDto;
import work.work4.pojo.Follow;
import work.work4.pojo.Friend;

import java.util.List;

public interface SocialServiceInterface{
    void followAction(FollowDto followDto);
    List<Follow> getFollowList(String userId, Integer pageNum, Integer pageSize);
    List<Follow> getFanList(String userId, Integer pageNum, Integer pageSize);
    List<Friend> getFriendList(String userId,Integer pageNum, Integer pageSize);
}
