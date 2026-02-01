package work.work4.controller;

import org.springframework.web.bind.annotation.*;
import work.work4.common.Result;
import work.work4.pojo.User;

import java.util.ArrayList;
import java.util.List;

@RestController
public class SocialController {
//    关注
    @PostMapping("/following/action")
    public Result followAction(@RequestParam String userId) {
        return Result.success();
    }
    @GetMapping("/following/list")
    public Result getFollowList(@RequestParam String userId,
                                      @RequestParam Integer pageNum,
                                      @RequestParam Integer pageSize) {
        List<User> userList=new ArrayList<>();
        return Result.success(userList);
    }
//    粉丝
    @GetMapping("/follower/list")
    public Result getfanList(@RequestParam String userId,
                                    @RequestParam Integer pageNum,
                                    @RequestParam Integer pageSize) {

        List<User> userList=new ArrayList<>();
        return Result.success(userList);
    }
//    好友
    @GetMapping("/friends/list")
    public Result getFriends(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        List<User> userList=new ArrayList<>();
        return Result.success(userList);
    }
}
