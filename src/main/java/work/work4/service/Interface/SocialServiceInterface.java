package work.work4.service.Interface;

import work.work4.vo.FollowVo;

import java.util.List;

public interface SocialServiceInterface{
    void followAction(String toUserId,String actionType);
    List<FollowVo> getFollowList(String userId, Integer pageNum, Integer pageSize);
    List<FollowVo> getFanList(String userId, Integer pageNum, Integer pageSize);
    List<FollowVo> getFriendList(Integer pageNum, Integer pageSize);
}
