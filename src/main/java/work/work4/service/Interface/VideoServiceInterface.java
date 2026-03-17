package work.work4.service.Interface;

import org.springframework.web.multipart.MultipartFile;
import work.work4.common.LoginUser;
import work.work4.common.RestBean;
import work.work4.vo.VideoVo;

import java.io.IOException;
import java.util.List;

public interface VideoServiceInterface {
    RestBean<Object> getVideoStream(String latest_time);
    void publish(MultipartFile file, String title, String description, LoginUser loginUser) throws IOException;
    RestBean<Object> getVideoList(String userId, Integer pageNum, Integer pageSize);

    RestBean<Object> getPopularVideo(Integer pageSize, Integer pageNum);

    RestBean<Object> searchVideo(String keywords,Integer pageSize,Integer pageNum,String username);
}
