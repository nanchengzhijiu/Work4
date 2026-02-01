package work.work4.controller;

import org.springframework.web.bind.annotation.*;
import work.work4.common.Result;
import work.work4.pojo.Search;
import work.work4.pojo.Video;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/video")
public class VideoController {
    @PostMapping("/publish")
    public Result publish(@RequestBody Video video) {
        return Result.success();
    }
    @GetMapping("/list")
    public Result list(@RequestParam String userId,
                            @RequestParam Integer pageNum,
                            @RequestParam Integer pageSize) {
        List<Video> videoList=new ArrayList<>();
        return Result.success(videoList);
    }
    @PostMapping("/search")
    public Result search(@RequestBody Search search) {
        List<Video> videoList=new ArrayList<>();
        return Result.success(videoList);
    }
    @GetMapping("/popular")
    public Result popular(@RequestParam Integer pageSize,
                               @RequestParam Integer pageNum) {
        List<Video> videoList=new ArrayList<>();
        return Result.success(videoList);
    }
}
