package work.work4.Controller;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import work.work4.Mappers.UserMapper;
import work.work4.pojo.User;
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    UserMapper mapper;
    @PostMapping("/register")
    public void register(@RequestBody User user) {

    }
    @PostMapping("/login")
    public void login(@RequestBody User user) {

    }
    @GetMapping("/info")
    public User getUserInfo(@RequestParam String id) {
        return null;
    }
    @PutMapping("/avatar/upload")
    public User getUserAvatar() {
        return null;
    }
}
