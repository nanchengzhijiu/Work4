package work.work4.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import work.work4.service.Interface.UserServiceInterface;
import work.work4.pojo.User;
@Service
public class UserService implements UserServiceInterface {
    @Override
    public void register(User user) {

    }

    @Override
    public void login(User user) {

    }

    @Override
    public User getUser(String userId) {
        return null;
    }

    @Override
    public void uploadAvatar(MultipartFile file) {

    }
}
