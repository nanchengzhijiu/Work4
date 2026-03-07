package work.work4.controller;

import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import work.work4.common.LoginUser;
import work.work4.common.RestBean;
import work.work4.common.Result;
import work.work4.dto.SearchDto;
import work.work4.dto.VideoUploadDto;
import work.work4.pojo.Video;
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
        return RestBean.success(videoService.getVideoStream(latest_time));
    }
    @PostMapping("/publish")
    public RestBean<Object> publish(@RequestParam MultipartFile data, @RequestParam String title, @RequestParam String description) throws IOException {
        // 1. 验证登录状态
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("用户未登录或认证失效");
        }
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        videoService.publish(data,title,description,loginUser);
        return RestBean.success();
    }
    @GetMapping("/list")
    public RestBean<Object> list(@RequestParam("user_id") String userId,
                            @RequestParam("page_num") Integer pageNum,
                            @RequestParam("page_size") Integer pageSize) {
        List<VideoVo> videoVoList=videoService.getVideoList(userId,pageNum,pageSize);
        return RestBean.success(videoVoList);
    }
    @PostMapping("/search")
    public Result search(@RequestBody SearchDto searchDto) {
        List<Video> videoList=videoService.searchVideo(searchDto);
        return Result.success(videoList);
    }
    @GetMapping("/popular")
    public Result popular(@RequestParam("page_size") Integer pageSize,
                               @RequestParam("page_num") Integer pageNum) {
        List<Video> videoList=videoService.getPopularVideo(pageSize,pageNum);
        return Result.success(videoList);
    }
}
