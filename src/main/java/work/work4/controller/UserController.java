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
    UserService userService;
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        userService.register(user);
        return Result.success();
    }
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        return Result.success(user);
    }
    @GetMapping("/info")
    public Result getUserInfo(@RequestParam String id) {
        User user=new User();
        return Result.success(user);
    }
    @PutMapping("/avatar/upload")
    public Result uploadUserAvatar() {
        User user=new User();
        return Result.success();
    }
}
