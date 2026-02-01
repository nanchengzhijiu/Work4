package work.work4.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import work.work4.common.Result;
import work.work4.pojo.User;
import work.work4.service.SocialService;

import java.util.ArrayList;
import java.util.List;

@RestController
public class SocialController {
    @Resource
    private SocialService socialService;
//    关注
    @PostMapping("/following/action")
    public Result followAction(@RequestParam String userId) {
        socialService.followAction();
        return Result.success();
    }
    @GetMapping("/following/list")
    public Result getFollowList(@RequestParam Integer userId,
                                      @RequestParam Integer pageNum,
                                      @RequestParam Integer pageSize) {
        List<User> userList=socialService.getFollowList(userId,pageNum,pageSize);
        return Result.success(userList);
    }
//    粉丝
    @GetMapping("/follower/list")
    public Result getFanList(@RequestParam Integer userId,
                                    @RequestParam Integer pageNum,
                                    @RequestParam Integer pageSize) {

        List<User> userList=socialService.getFanList(userId,pageNum,pageSize);
        return Result.success(userList);
    }
//    好友
    @GetMapping("/friends/list")
    public Result getFriends(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        List<User> userList=socialService.getFriendList(pageNum,pageSize);
        return Result.success(userList);
    }
}
