package work.work4.service.Interface;

import org.springframework.web.multipart.MultipartFile;
import work.work4.vo.UserVo;

import java.io.IOException;

public interface UserServiceInterface {
    void register(String username, String password);
    UserVo login(String username, String password);
    UserVo getUser(String userId);
    UserVo uploadAvatar(MultipartFile file) throws IOException;
}
