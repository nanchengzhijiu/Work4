package work.work4.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import work.work4.dto.SearchDto;
import work.work4.dto.VideoUploadDto;
import work.work4.mapper.VideoMapper;
import work.work4.service.Interface.VideoServiceInterface;
import work.work4.pojo.Video;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
@Service
public class VideoService implements VideoServiceInterface {
    @Resource
    private VideoMapper videoMapper;
    @Async("videoPublishExecutor")
    @Override
    public void publish(VideoUploadDto videoDto) throws IOException {
        String pathName="static/video";
        File directory = new File(pathName);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new IOException("无法创建目录: " + directory);
            }
        }
        //上传文件名
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))+videoDto.getVideoFile().getOriginalFilename();
        // 构建目标文件路径
        Path targetLocation = Paths.get(pathName).resolve(fileName);
        // 保存文件
        try (InputStream inputStream = videoDto.getVideoFile().getInputStream()) {
            //覆盖相同文件
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }
        Video video = new Video();
        video.setTitle(videoDto.getTitle());
        video.setDescription(videoDto.getDescription());
        video.setVideoUrl(videoDto.getVideoFile().getOriginalFilename());
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
    public List<Video> searchVideo(SearchDto searchDto) {
        Page<Video> page = new Page<>(searchDto.getPageNum(), searchDto.getPageSize());
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.eq("title", searchDto.getKeyword());
        return videoMapper.selectPage(page,wrapper).getRecords();
    }
}
