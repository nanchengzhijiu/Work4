package work.work4.service.Interface;

import work.work4.pojo.Search;
import work.work4.pojo.Video;

import java.util.List;

public interface VideoServiceInterface {
    public void publish(Video video);
    public List<Video> getVideoList(Integer userId, Integer pageNum, Integer pageSize);
    public List<Video> getPopularVideo();
    public List<Video> searchVideo(Search search);
}
