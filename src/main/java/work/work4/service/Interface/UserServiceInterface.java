package work.work4.service.Interface;

import org.springframework.web.multipart.MultipartFile;
import work.work4.common.RestBean;
import work.work4.vo.UserVo;

import java.io.IOException;

public interface UserServiceInterface {
    void register(String username, String password);
    RestBean<Object> login(String username, String password);
    RestBean<Object> getUser(String userId,String token);
    RestBean<Object> uploadAvatar(MultipartFile file,String token) throws IOException;
}
