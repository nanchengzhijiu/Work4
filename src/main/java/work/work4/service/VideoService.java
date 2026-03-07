package work.work4.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import work.work4.common.LoginUser;
import work.work4.dto.SearchDto;
import work.work4.dto.VideoUploadDto;
import work.work4.mapper.SearchMapper;
import work.work4.mapper.VideoMapper;
import work.work4.pojo.Search;
import work.work4.service.Interface.VideoServiceInterface;
import work.work4.pojo.Video;
import work.work4.util.FileUtils;
import work.work4.util.VideoMetadataUtil;
import work.work4.vo.VideoVo;

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
import java.util.stream.Collectors;

@Service
public class VideoService implements VideoServiceInterface {
    @Resource
    private VideoMapper videoMapper;
    @Resource
    private SearchMapper searchMapper;
    @Resource
    private FileUtils fileUtils;
    // Redis key常量
    private static final String VIDEO_VISIT_RANKING = "video:ranking:visit";// 点击率排行榜
    private static final String VIDEO_POOL_KEY = "video:global:pool";//随机池
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

    @Override
    public List<VideoVo> getVideoStream(String latest_time) {
        // 1. 从 Redis 随机抽取 20 个视频 ID
        List<Object> randomIds = redisTemplate.opsForSet().randomMembers(VIDEO_POOL_KEY, 20);

        if (randomIds == null || randomIds.isEmpty()) {
            // 如果 Redis 为空（初次启动），可以从数据库保底查几条
            return null;
        }
        // 2. 根据 ID 批量查询数据库详情
        List<Video> videos = videoMapper.selectVideosByIdList(randomIds);
        List<VideoVo> videoVos = videos.stream().map(video -> {
            VideoVo vo = new VideoVo();
            BeanUtils.copyProperties(video, vo);
            return vo;
        }).toList();
        return videoVos;
    }

    @Override
    public void publish(MultipartFile file,String title,String description,LoginUser loginUser) throws IOException {

        // 2. 调用工具类上传到阿里云 OSS
        // 数组索引 0 是视频名，1 是带截帧参数的封面名
        String[] uploadResults = fileUtils.uploadVideo(file);
        String videoFileName = uploadResults[0];
        String coverFileName = uploadResults[1];

        // 3. 构建实体并入库
        Video video = new Video()
                .setUserId(loginUser.getUser().getId())
                .setTitle(title)
                .setDescription(description)
                .setVideoUrl(videoFileName)
                .setCoverUrl(coverFileName);
        videoMapper.insert(video);
        redisTemplate.opsForSet().add(VIDEO_POOL_KEY, video.getId());
    }
    @Override
    public List<VideoVo> getVideoList(String userId, Integer pageNum, Integer pageSize) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<Video> videos = videoMapper.selectPage(page,wrapper).getRecords();
        List<VideoVo> videoVos = videos.stream().map(video -> {
            VideoVo vo = new VideoVo();
            BeanUtils.copyProperties(video, vo);
            return vo;
        }).toList();
        return videoVos;
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
                        new DefaultTypedTuple<>(video.getId(), video.getVisitCount().doubleValue());
                tuples.add(tuple);
            }
            redisTemplate.opsForZSet().add(VIDEO_VISIT_RANKING, tuples);
            // 设置过期时间（7天）
            redisTemplate.expire(VIDEO_VISIT_RANKING, 7, TimeUnit.DAYS);
        }
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
