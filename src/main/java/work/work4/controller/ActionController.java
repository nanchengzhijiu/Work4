package work.work4.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import work.work4.common.RestBean;
import work.work4.common.Result;
import work.work4.dto.CommentDto;
import work.work4.pojo.Comment;
import work.work4.pojo.Video;
import work.work4.service.ActionService;
import java.util.List;

@RestController
public class ActionController {
    @Resource
    private ActionService actionService;
//    点赞
    @PostMapping("/like/action")
    public RestBean<Object> action(@RequestParam("video_id") String videoId,
                           @RequestParam("comment_id") String commentId,
                           @RequestParam("action_type") String actionType) {
        actionService.likeAction(videoId,commentId,actionType);
        return RestBean.success();
    }
    @GetMapping("like/list")
    public Result list(@RequestParam String userId,
                            @RequestParam Integer pageSize,
                            @RequestParam Integer pageNum) {
        List<Video> videoList=actionService.getLikeList(userId,pageSize,pageNum);
        return Result.success(videoList);
    }
//    评论
    @PostMapping("/comment/publish")
    public Result publishComment(@RequestBody CommentDto commentDto) {
        actionService.comment(commentDto);
        return Result.success();
    }
    @GetMapping("/comment/list")
    public Result listComment(@RequestParam String videoId,
                                     @RequestParam String commentId,
                                     @RequestParam Integer pageSize,
                                     @RequestParam Integer pageNum) {
        List<Comment> commentList=actionService.getCommentList(videoId,commentId,pageSize,pageNum);
        return Result.success(commentList);
    }
    @DeleteMapping("/comment/delete")
    public Result deleteComment(@RequestBody CommentDto commentDto) {
        actionService.deleteComment(commentDto);
        return Result.success();
    }
}
