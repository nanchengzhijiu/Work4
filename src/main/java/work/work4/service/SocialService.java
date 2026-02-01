package work.work4.service;

import org.springframework.stereotype.Service;
import work.work4.service.Interface.SocialServiceInterface;
import work.work4.pojo.User;

import java.util.List;
@Service
public class SocialService implements SocialServiceInterface {
    @Override
    public void follow() {

    }

    @Override
    public List<User> getFollowList() {
        return List.of();
    }

    @Override
    public List<User> getFanList() {
        return List.of();
    }

    @Override
    public List<User> getFriendList() {
        return List.of();
    }
}
