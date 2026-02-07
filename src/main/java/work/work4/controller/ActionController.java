package work.work4.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import work.work4.common.Result;
import work.work4.pojo.Comment;
import work.work4.pojo.Like;
import work.work4.pojo.Video;
import work.work4.service.ActionService;
import java.util.List;

@RestController
public class ActionController {
    @Resource
    private ActionService actionService;
    @GetMapping("/test")
    public String testS(){
        return "HelloWorld";
    }
//    点赞
    @PostMapping("/like/action")
    public Result action(@RequestBody Like like,@RequestParam String actionType) {
        actionService.likeAction(like,actionType);
        return Result.success();
    }
    @GetMapping("like/list")
    public Result list(@RequestParam Long userId,
                            @RequestParam Integer pageSize,
                            @RequestParam Integer pageNum) {
        List<Video> videoList=actionService.getLikeList(userId,pageSize,pageNum);
        return Result.success(videoList);
    }
//    评论
    @PostMapping("/comment/publish")
    public Result publishComment(@RequestBody Comment comment) {
        actionService.comment(comment);
        return Result.success();
    }
    @GetMapping("/comment/list")
    public Result listComment(@RequestParam Long videoId,
                                     @RequestParam Integer pageSize,
                                     @RequestParam Integer pageNum) {
        List<Comment> commentList=actionService.getCommentList(videoId,pageSize,pageNum);
        return Result.success(commentList);
    }
    @DeleteMapping("/comment/delete")
    public Result deleteComment(@RequestBody Comment comment) {
        actionService.deleteComment(comment);
        return Result.success();
    }
}
