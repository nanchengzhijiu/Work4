package work.work4.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import work.work4.common.RestBean;
import work.work4.common.Result;
import work.work4.dto.CommentDto;
import work.work4.pojo.Comment;
import work.work4.pojo.Video;
import work.work4.service.ActionService;
import work.work4.vo.CommentVo;
import work.work4.vo.VideoVo;

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
    public RestBean<Object> list(@RequestParam("user_id") String userId,
                            @RequestParam("page_size") Integer pageSize,
                            @RequestParam("page_num") Integer pageNum) {
        List<VideoVo> videoList=actionService.getLikeList(userId,pageSize,pageNum);
        return RestBean.success(videoList);
    }
//    评论
    @PostMapping("/comment/publish")
    public RestBean<Object> publishComment(@RequestParam("video_id")String videoId,@RequestParam("comment_id")String commentId,@RequestParam("content")String content) {
        actionService.comment(videoId,commentId,content);
        return RestBean.success();
    }
    @GetMapping("/comment/list")
    public RestBean<Object> listComment(@RequestParam("video_id") String videoId,
                                     @RequestParam("comment_id") String commentId,
                                     @RequestParam("page_size") Integer pageSize,
                                     @RequestParam("page_num") Integer pageNum) {
        List<CommentVo> commentVoList=actionService.getCommentList(videoId,commentId,pageSize,pageNum);
        return RestBean.success(commentVoList);
    }
    @DeleteMapping("/comment/delete")
    public RestBean<Object> deleteComment(@RequestParam("video_id")String videoId,@RequestParam("comment_id")String commentId) {
        actionService.deleteComment(videoId,commentId);
        return RestBean.success();
    }
}
