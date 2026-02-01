package work.work4.service.Interface;

import work.work4.pojo.User;

import java.util.List;

public interface SocialServiceInterface {
    public void follow();
    public List<User> getFollowList();
    public List<User> getFanList();
    public List<User> getFriendList();
}
