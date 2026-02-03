package work.work4.service.Interface;

import work.work4.pojo.User;

public interface UserServiceInterface {
    void register(User user);
    void login(User user);
    User getUser(Long userId);
    void uploadAvatar();
}
