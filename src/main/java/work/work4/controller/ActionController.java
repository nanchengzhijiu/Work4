package work.work4.controller;

import org.springframework.web.bind.annotation.*;
import work.work4.common.Result;
import work.work4.pojo.Comment;
import work.work4.pojo.Search;
import work.work4.pojo.Video;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ActionController {
//    点赞
    @PostMapping("/like/action")
    public Result action() {
        return Result.success();
    }
    @GetMapping("like/list")
    public Result list(@RequestParam String userId,
                            @RequestParam Integer pageSize,
                            @RequestParam Integer pageNum) {
        List<Video> videoList=new ArrayList<>();
        return Result.success(videoList);
    }
//    评论
    @PostMapping("/comment/publish")
    public Result publishComment(@RequestBody Comment comment) {
        return Result.success();
    }
    @GetMapping("/comment/list")
    public Result listComment(@RequestParam String videoId,
                                     @RequestParam String commentId,
                                     @RequestParam Integer pageSize,
                                     @RequestParam Integer pageNum) {
        List<Comment> commentList=new ArrayList<>();
        return Result.success(commentList);
    }
    @DeleteMapping("/comment/delete")
    public Result deleteComment(@RequestBody Comment comment) {
        return Result.success();
    }
}
