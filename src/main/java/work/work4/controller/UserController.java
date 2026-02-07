package work.work4.controller;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import work.work4.common.Result;
import work.work4.pojo.User;
import work.work4.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        userService.register(user);
        return Result.success();
    }
//    @PostMapping("/login")
//    public Result login(@RequestBody User user) {
//        userService.login(user);
//        return Result.success();
//    }
    @GetMapping("/info")
    public Result getUserInfo(@RequestParam Long id) {
        User user=userService.getUser(id);
        return Result.success(user);
    }
    @PutMapping("/avatar/upload")
    public Result uploadUserAvatar() {
        userService.uploadAvatar();
        return Result.success();
    }
}
