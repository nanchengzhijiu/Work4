package work.work4.service.Interface;

import org.springframework.scheduling.annotation.Async;
import work.work4.entity.Search;
import work.work4.pojo.Video;

import java.util.List;

public interface VideoServiceInterface {
    @Async("videoPublishExecutor")
    void publish(Video video);
    List<Video> getVideoList(Long userId, Integer pageNum, Integer pageSize);

    List<Video> getPopularVideo(Integer pageSize, Integer pageNum);

    List<Video> searchVideo(Search search);
}
