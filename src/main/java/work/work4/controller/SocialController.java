package work.work4.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import work.work4.common.RestBean;
import work.work4.service.SocialService;
import work.work4.vo.FollowVo;

import java.util.List;

@RestController
public class SocialController {
    @Resource
    private SocialService socialService;
    @GetMapping("/admin/test")
    public String test(){
        return "管理员页面";
    }
//    关注
    @PostMapping("/relation/action")
    public RestBean<Object> followAction(@RequestParam("to_user_id") String toUserId,
                                         @RequestParam("action_type") String actionType) {
        socialService.followAction(toUserId,actionType);
        return RestBean.success();
    }
    @GetMapping("/following/list")
    public RestBean<Object> getFollowList(@RequestParam("user_id") String userId,
                                      @RequestParam("page_num") Integer pageNum,
                                      @RequestParam("page_size") Integer pageSize) {
        List<FollowVo> userList=socialService.getFollowList(userId,pageNum,pageSize);
        return RestBean.success(userList);
    }
//    粉丝
    @GetMapping("/follower/list")
    public RestBean<Object> getFanList(@RequestParam("user_id") String userId,
                                    @RequestParam("page_num") Integer pageNum,
                                    @RequestParam("page_size") Integer pageSize) {

        List<FollowVo> userList=socialService.getFanList(userId,pageNum,pageSize);
        return RestBean.success(userList);
    }
//    好友
    @GetMapping("/friends/list")
    public RestBean<Object> getFriends(@RequestParam("page_num") Integer pageNum,
                                       @RequestParam("page_size") Integer pageSize) {
        List<FollowVo> userList=socialService.getFriendList(pageNum,pageSize);
        return RestBean.success(userList);
    }
}
