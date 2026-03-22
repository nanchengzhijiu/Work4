package work.work4.controller;

import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import work.work4.common.LoginUser;
import work.work4.common.RestBean;
import work.work4.service.VideoService;
import work.work4.vo.VideoVo;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/video")
public class VideoController {
    @Resource
    private VideoService videoService;
    @GetMapping("/feed")
    public RestBean<Object> getVideoStream(@RequestParam String latest_time){
        return videoService.getVideoStream(latest_time);
    }
    @PostMapping("/publish")
    public RestBean<Object> publish(@RequestParam MultipartFile data, @RequestParam String title, @RequestParam String description) throws IOException {
        // 1. 验证登录状态
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        videoService.publish(data,title,description,loginUser);
        return RestBean.success();
    }
    @GetMapping("/list")
    public RestBean<Object> list(@RequestParam("user_id") String userId,
                            @RequestParam("page_num") Integer pageNum,
                            @RequestParam("page_size") Integer pageSize) {
        return videoService.getVideoList(userId,pageNum,pageSize);
    }
    @PostMapping("/search")
    public RestBean<Object> search(@RequestParam String keywords,
                         @RequestParam("page_size") Integer pageSize,
                         @RequestParam("page_num")Integer pageNum,
                         @RequestParam String username) {
        return videoService.searchVideo(keywords,pageSize,pageNum,username);
    }
    @GetMapping("/popular")
    public RestBean<Object> popular(@RequestParam("page_size") Integer pageSize,
                               @RequestParam("page_num") Integer pageNum) {
        return videoService.getPopularVideo(pageSize,pageNum);
    }
}
