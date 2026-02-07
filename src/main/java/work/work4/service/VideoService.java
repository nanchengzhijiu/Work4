package work.work4.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import work.work4.mapper.VideoMapper;
import work.work4.service.Interface.VideoServiceInterface;
import work.work4.entity.Search;
import work.work4.pojo.Video;

import java.util.List;
@Service
public class VideoService implements VideoServiceInterface {
    @Resource
    private VideoMapper videoMapper;
    @Async("videoPublishExecutor")
    @Override
    public void publish(Video video) {
        videoMapper.insert(video);
    }
    @Override
    public List<Video> getVideoList(Long userId, Integer pageNum, Integer pageSize) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return videoMapper.selectPage(page,wrapper).getRecords();
    }

    @Override
    public List<Video> getPopularVideo(Integer pageSize, Integer pageNum) {
        return List.of();
    }

    @Override
    public List<Video> searchVideo(Search search) {
        Page<Video> page = new Page<>(search.getPageNum(), search.getPageSize());
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.eq("title",search.getKeyword());
        return videoMapper.selectPage(page,wrapper).getRecords();
    }
}
