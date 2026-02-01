package work.work4.service.Interface;

import org.springframework.web.multipart.MultipartFile;
import work.work4.pojo.User;

public interface UserServiceInterface {
    public void register(User user);
    public void login(User user);
    public User getUser(String userId);
    public void uploadAvatar();
}
