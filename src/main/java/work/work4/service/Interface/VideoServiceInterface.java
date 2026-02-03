package work.work4.service.Interface;

import work.work4.entity.Search;
import work.work4.pojo.Video;

import java.util.List;

public interface VideoServiceInterface {
    void publish(Video video);
    List<Video> getVideoList(Long userId, Integer pageNum, Integer pageSize);

    List<Video> getPopularVideo(Integer pageSize, Integer pageNum);

    List<Video> searchVideo(Search search);
}
