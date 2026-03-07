package work.work4.controller;
import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import work.work4.common.LoginUser;
import work.work4.common.RestBean;
import work.work4.service.UserService;
import work.work4.vo.UserVo;

import java.io.IOException;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    @PostMapping("/register")
    public RestBean<Object> register(@RequestParam String username, @RequestParam String password) {
        userService.register(username, password);
        return RestBean.success();
    }
    @PostMapping("/login")
    public RestBean<Object> login(@RequestParam String username, @RequestParam String password) {
        return RestBean.success(userService.login(username,password));
    }
    @GetMapping("/info")
    public RestBean<Object> getUserInfo(@RequestParam String user_id) {
        UserVo uservo=userService.getUser(user_id);
        return RestBean.success(uservo);
    }
    @PutMapping("/avatar/upload")
    public RestBean<Object> uploadUserAvatar(@RequestParam MultipartFile data) throws IOException {
        return RestBean.success(userService.uploadAvatar(data));
    }
}
