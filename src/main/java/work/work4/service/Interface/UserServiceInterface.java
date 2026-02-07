package work.work4.service.Interface;

import org.springframework.web.multipart.MultipartFile;
import work.work4.pojo.User;

import java.io.IOException;

public interface UserServiceInterface {
    void register(User user);
//    void login(User user);
    User getUser(Long userId);
    void uploadAvatar(MultipartFile file) throws IOException;
}
