package work.work4.Controller;

import org.springframework.web.bind.annotation.*;
import work.work4.pojo.Follow;
import work.work4.pojo.User;

import java.util.List;

@RestController
public class SocialController {
//    关注
    @PostMapping("/following/action")
    public void followAction(@RequestBody Follow follow) {

    }
    @GetMapping("/following/list")
    public List<Follow> getFollowList(@RequestParam String userId,
                                      @RequestParam Integer pageNum,
                                      @RequestParam Integer pageSize) {
        return null;
    }
//    粉丝
    @GetMapping("/follower/list")
    public List<User> getfanList(@RequestParam String userId,
                                    @RequestParam Integer pageNum,
                                    @RequestParam Integer pageSize) {
        return null;
    }
//    好友
    @GetMapping("/friends/list")
    public List<User> getFriends(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        return null;
    }
}
