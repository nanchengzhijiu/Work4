package work.work4.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import work.work4.dto.SearchDto;
import work.work4.dto.VideoUploadDto;
import work.work4.mapper.SearchMapper;
import work.work4.mapper.VideoMapper;
import work.work4.pojo.Search;
import work.work4.service.Interface.VideoServiceInterface;
import work.work4.pojo.Video;
import work.work4.util.VideoMetadataUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class VideoService implements VideoServiceInterface {
    @Resource
    private VideoMapper videoMapper;
    @Resource
    private SearchMapper searchMapper;
    // Redis key常量
    private static final String VIDEO_VISIT_RANKING = "video:ranking:visit";// 点击率排行榜
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @PostConstruct
    public void initRanking() {
        // 加载点击率排行榜
        refreshVisitRanking();
    }
//    确保目录存在
    private void ensureDirectoryExists(String path) throws IOException {
        File directory = new File(path);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("无法创建目录: " + path);
        }
    }
    @Async("videoPublishExecutor")
    @Override
    public void publish(VideoUploadDto videoDto) throws IOException {
        // 1. 基础路径配置
        String baseDir = "static/video/";
        String coverDir = "static/cover/";
        ensureDirectoryExists(baseDir);
        ensureDirectoryExists(coverDir);

        // 2. 生成唯一文件名 (建议加 UUID 防止并发冲突)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String originalName = videoDto.getVideoFile().getOriginalFilename();
        String videoFileName = timestamp + "_" + originalName;
        String coverFileName = videoFileName.substring(0, videoFileName.lastIndexOf(".")) + ".jpg";

        Path videoPath = Paths.get(baseDir).resolve(videoFileName);
        Path coverPath = Paths.get(coverDir).resolve(coverFileName);

        // 3. 保存视频文件到本地
        try (InputStream inputStream = videoDto.getVideoFile().getInputStream()) {
            Files.copy(inputStream, videoPath, StandardCopyOption.REPLACE_EXISTING);
        }
        // 4. 自动识别时长与截取第一帧封面
        long duration = VideoMetadataUtil.processVideo(
                videoPath.toAbsolutePath().toString(), // 传入视频的绝对路径
                coverPath.toAbsolutePath().toString()       // 传入封面图的绝对路径
        );
        // 5. 保存到数据库
        Video video = new Video()
                .setTitle(videoDto.getTitle())
                .setDescription(videoDto.getDescription())
                .setVideoUrl(videoFileName)         // 建议存生成的唯一文件名
                .setCoverUrl(coverFileName)         // 新增：封面图路径
                .setDuring(duration)// 新增：时长
                .setType(videoDto.getType())
                .setYear(videoDto.getYear())
                .setUserId(videoDto.getUserId());
        videoMapper.insert(video);
    }
    @Override
    public List<Video> getVideoList(Long userId, Integer pageNum, Integer pageSize) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return videoMapper.selectPage(page,wrapper).getRecords();
    }
    public void refreshVisitRanking() {
        // 1. 查询数据库，按点击率排序，取前1000
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Video::getVisitCount)
                .last("LIMIT 1000");

        List<Video> topVideos = videoMapper.selectList(wrapper);

        // 2. 清空旧的排行榜
        redisTemplate.delete(VIDEO_VISIT_RANKING);

        // 3. 批量添加到Sorted Set
        if (!CollectionUtils.isEmpty(topVideos)) {
            Set<ZSetOperations.TypedTuple<Object>> tuples = new HashSet<>();
            for (Video video : topVideos) {
                // 使用视频ID作为member，点击率作为score
                ZSetOperations.TypedTuple<Object> tuple =
                        new DefaultTypedTuple<>(video.getId().toString(), video.getVisitCount().doubleValue());
                tuples.add(tuple);
            }
            redisTemplate.opsForZSet().add(VIDEO_VISIT_RANKING, tuples);
            // 设置过期时间（7天）
            redisTemplate.expire(VIDEO_VISIT_RANKING, 7, TimeUnit.DAYS);
        }
        System.out.println("点击率排行榜刷新完成，共 " + topVideos.size() + " 个视频");
    }
    @Override
    public List<Video> getPopularVideo(Integer pageSize, Integer pageNum) {
//      获取排行版前十
        Set<Object> topVideos = redisTemplate.opsForZSet()
                .reverseRange(VIDEO_VISIT_RANKING, (pageNum-1)*pageSize, pageSize);
        if (CollectionUtils.isEmpty(topVideos)) {
            refreshVisitRanking();
        }
        ArrayList<Video> topVideoList = new ArrayList<>();
        topVideos.forEach(video -> {
            topVideoList.add(videoMapper.selectById(video.toString()));
        });
        return topVideoList;
    }

    @Override
    public List<Video> searchVideo(SearchDto searchDto) {
        Page<Video> page = new Page<>(searchDto.getPageNum(), searchDto.getPageSize());
        String keyword = searchDto.getKeyword();
        String type = searchDto.getType();
        LocalDateTime publishTime = searchDto.getPublishTime();
        String year = searchDto.getYear();
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.eq("title", keyword)
                .eq("type", type)
                .eq("publish_time", publishTime)
                .eq("year", year);
        Search search = new Search()
                        .setKeyword(keyword)
                        .setType(type)
                        .setPublishTime(publishTime)
                        .setYear(year);
        searchMapper.insert(search);
        return videoMapper.selectPage(page,wrapper).getRecords();
    }
}
