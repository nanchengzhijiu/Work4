package work.work4.service.Interface;

import work.work4.pojo.Follow;
import work.work4.pojo.Friend;

import java.util.List;

public interface SocialServiceInterface{
    void followAction(Long userId,Long followId,String actionType);
    List<Follow> getFollowList(Long userId, Integer pageNum, Integer pageSize);
    List<Follow> getFanList(Long userId, Integer pageNum, Integer pageSize);
    List<Friend> getFriendList(Long userId,Integer pageNum, Integer pageSize);
}
