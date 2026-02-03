package work.work4.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import work.work4.common.Result;
import work.work4.pojo.Follow;
import work.work4.pojo.Friend;
import work.work4.service.SocialService;
import java.util.List;

@RestController
public class SocialController {
    @Resource
    private SocialService socialService;
//    关注
    @PostMapping("/following/action")
    public Result followAction(@RequestParam Long userId,@RequestParam Long followId,@RequestParam String actionType) {
        socialService.followAction(userId,followId,actionType);
        return Result.success();
    }
    @GetMapping("/following/list")
    public Result getFollowList(@RequestParam Long userId,
                                      @RequestParam Integer pageNum,
                                      @RequestParam Integer pageSize) {
        List<Follow> userList=socialService.getFollowList(userId,pageNum,pageSize);
        return Result.success(userList);
    }
//    粉丝
    @GetMapping("/follower/list")
    public Result getFanList(@RequestParam Long userId,
                                    @RequestParam Integer pageNum,
                                    @RequestParam Integer pageSize) {

        List<Follow> userList=socialService.getFanList(userId,pageNum,pageSize);
        return Result.success(userList);
    }
//    好友
    @GetMapping("/friends/list")
    public Result getFriends(@RequestParam Long userId,@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        List<Friend> userList=socialService.getFriendList(userId,pageNum,pageSize);
        return Result.success(userList);
    }
}
