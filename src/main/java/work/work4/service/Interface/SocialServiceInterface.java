package work.work4.service.Interface;

import work.work4.pojo.User;

import java.util.List;

public interface SocialServiceInterface {
    public void followAction();
    public List<User> getFollowList(Integer userId, Integer pageNum, Integer pageSize);
    public List<User> getFanList(Integer userId, Integer pageNum, Integer pageSize);
    public List<User> getFriendList(Integer pageNum, Integer pageSize);
}
