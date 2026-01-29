package work.work4.Controller;

import org.springframework.web.bind.annotation.*;
import work.work4.pojo.Comment;
import work.work4.pojo.Search;
import work.work4.pojo.Video;

import java.util.List;

@RestController
public class ActionController {
//    点赞
    @PostMapping("/like/action")
    public void action(@RequestBody Search search) {

    }
    @GetMapping("like/list")
    public List<Video> list(@RequestParam String userId,
                            @RequestParam Integer pageSize,
                            @RequestParam Integer pageNum) {
        return null;
    }
//    评论
    @PostMapping("/comment/publish")
    public void publishComment(@RequestBody Comment comment) {

    }
    @GetMapping("/comment/list")
    public List<Comment> listComment(@RequestParam String videoId,
                                     @RequestParam String commentId,
                                     @RequestParam Integer pageSize,
                                     @RequestParam Integer pageNum) {
        return null;
    }
    @DeleteMapping("/comment/delete")
    public void deleteComment(@RequestBody Comment comment) {

    }
}
