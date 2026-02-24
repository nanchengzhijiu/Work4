package work.work4.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import work.work4.common.Result;
import work.work4.dto.CommentDto;
import work.work4.dto.LikeDto;
import work.work4.pojo.Comment;
import work.work4.pojo.Video;
import work.work4.service.ActionService;
import java.util.List;

@RestController
public class ActionController {
    @Resource
    private ActionService actionService;
    @GetMapping("/test")
    public String test(){
        return "HelloWorld";
    }
//    点赞
    @PostMapping("/like/action")
    public Result action(@RequestBody LikeDto likeDto) {
        actionService.likeAction(likeDto);
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
    public Result publishComment(@RequestBody CommentDto commentDto) {
        actionService.comment(commentDto);
        return Result.success();
    }
    @GetMapping("/comment/list")
    public Result listComment(@RequestParam Long videoId,
                                     @RequestParam Long commentId,
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
