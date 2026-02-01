package work.work4.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import work.work4.common.Result;
import work.work4.pojo.Comment;
import work.work4.pojo.User;
import work.work4.service.ActionService;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ActionController {
    @Resource
    private ActionService actionService;
//    点赞
    @PostMapping("/like/action")
    public Result action() {
        actionService.likeAction();
        return Result.success();
    }
    @GetMapping("like/list")
    public Result list(@RequestParam String userId,
                            @RequestParam Integer pageSize,
                            @RequestParam Integer pageNum) {
        List<User> videoList=actionService.getLikeList();
        return Result.success(videoList);
    }
//    评论
    @PostMapping("/comment/publish")
    public Result publishComment(@RequestBody Comment comment) {
        actionService.comment();
        return Result.success();
    }
    @GetMapping("/comment/list")
    public Result listComment(@RequestParam String videoId,
                                     @RequestParam String commentId,
                                     @RequestParam Integer pageSize,
                                     @RequestParam Integer pageNum) {
        List<Comment> commentList=actionService.getCommentList();
        return Result.success(commentList);
    }
    @DeleteMapping("/comment/delete")
    public Result deleteComment(@RequestBody Comment comment) {
        actionService.deleteComment();
        return Result.success();
    }
}
