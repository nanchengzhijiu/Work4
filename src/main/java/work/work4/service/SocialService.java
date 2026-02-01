package work.work4.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import work.work4.service.Interface.SocialServiceInterface;
import work.work4.pojo.User;

import java.util.List;
@Service
public class SocialService implements SocialServiceInterface {
    @Override
    public void followAction() {

    }

    @Override
    public List<User> getFollowList(Integer userId,
                                    Integer pageNum,
                                    Integer pageSize) {
        return List.of();
    }

    @Override
    public List<User> getFanList(Integer userId, Integer pageNum, Integer pageSize) {
        return List.of();
    }

    @Override
    public List<User> getFriendList(Integer pageNum,Integer pageSize) {
        return List.of();
    }
}
