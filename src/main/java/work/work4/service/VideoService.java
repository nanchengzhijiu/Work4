package work.work4.service;

import org.springframework.stereotype.Service;
import work.work4.service.Interface.VideoServiceInterface;
import work.work4.pojo.Search;
import work.work4.pojo.Video;

import java.util.List;
@Service
public class VideoService implements VideoServiceInterface {
    @Override
    public void publish(Video video) {

    }

    @Override
    public List<Video> getVideoList(Integer userId, Integer pageNum, Integer pageSize) {
        return List.of();
    }

    @Override
    public List<Video> getPopularVideo() {
        return List.of();
    }

    @Override
    public List<Video> searchVideo(Search search) {
        return List.of();
    }
}
