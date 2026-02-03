package work.work4.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import work.work4.common.Result;
import work.work4.entity.Search;
import work.work4.pojo.Video;
import work.work4.service.VideoService;

import java.util.List;

@RestController
@RequestMapping("/video")
public class VideoController {
    @Resource
    private VideoService videoService;
    @PostMapping("/publish")
    public Result publish(@RequestBody Video video) {
        videoService.publish(video);
        return Result.success();
    }
    @GetMapping("/list")
    public Result list(@RequestParam Long userId,
                            @RequestParam Integer pageNum,
                            @RequestParam Integer pageSize) {
        List<Video> videoList=videoService.getVideoList(userId,pageNum,pageSize);
        return Result.success(videoList);
    }
    @PostMapping("/search")
    public Result search(@RequestBody Search search) {
        List<Video> videoList=videoService.searchVideo(search);
        return Result.success(videoList);
    }
    @GetMapping("/popular")
    public Result popular(@RequestParam Integer pageSize,
                               @RequestParam Integer pageNum) {
        List<Video> videoList=videoService.getPopularVideo(pageSize,pageNum);
        return Result.success(videoList);
    }
}
