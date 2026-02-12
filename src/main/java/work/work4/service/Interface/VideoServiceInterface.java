package work.work4.service.Interface;

import org.springframework.scheduling.annotation.Async;
import work.work4.dto.SearchDto;
import work.work4.dto.VideoUploadDto;
import work.work4.pojo.Video;

import java.io.IOException;
import java.util.List;

public interface VideoServiceInterface {
    @Async("videoPublishExecutor")
    void publish(VideoUploadDto videoDto) throws IOException;
    List<Video> getVideoList(Long userId, Integer pageNum, Integer pageSize);

    List<Video> getPopularVideo(Integer pageSize, Integer pageNum);

    List<Video> searchVideo(SearchDto searchDto);
}
