package work.work4.service.Interface;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;
import work.work4.common.LoginUser;
import work.work4.dto.SearchDto;
import work.work4.dto.VideoUploadDto;
import work.work4.pojo.Video;
import work.work4.vo.VideoVo;

import java.io.IOException;
import java.util.List;

public interface VideoServiceInterface {
    List<VideoVo> getVideoStream(String latest_time);
    void publish(MultipartFile file, String title, String description, LoginUser loginUser) throws IOException;
    List<VideoVo> getVideoList(String userId, Integer pageNum, Integer pageSize);

    List<Video> getPopularVideo(Integer pageSize, Integer pageNum);

    List<Video> searchVideo(SearchDto searchDto);
}
