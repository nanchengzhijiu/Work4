package work.work4.Controller;

import org.springframework.web.bind.annotation.*;
import work.work4.pojo.Search;
import work.work4.pojo.Video;

import java.util.List;

@RestController
@RequestMapping("/video")
public class VideoController {
    @PostMapping("/publish")
    public void publish(@RequestBody Video video) {
    }
    @GetMapping("/list")
    public List<Video> list(@RequestParam String userId,
                            @RequestParam Integer pageNum,
                            @RequestParam Integer pageSize) {
        return null;
    }
    @PostMapping("/search")
    public List<Video> search(@RequestBody Search search) {
        return null;
    }
    @GetMapping("/popular")
    public List<Video> popular(@RequestParam Integer pageSize,
                               @RequestParam Integer pageNum) {
        return null;
    }
}
