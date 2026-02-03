package work.work4.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import work.work4.mapper.UserMapper;
import work.work4.service.Interface.UserServiceInterface;
import work.work4.pojo.User;
@Service
public class UserService implements UserServiceInterface {
    @Resource
    private UserMapper userMapper;
    @Override
    public void register(User user) {
        userMapper.insert(user);
    }

    @Override
    public void login(User user) {

    }

    @Override
    public User getUser(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public void uploadAvatar() {

    }
}
