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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import work.work4.common.LoginUser;
import work.work4.dto.SearchDto;
import work.work4.mapper.SearchMapper;
import work.work4.mapper.VideoMapper;
import work.work4.pojo.Search;
import work.work4.service.Interface.VideoServiceInterface;
import work.work4.pojo.Video;
import work.work4.util.FileUtils;
import work.work4.vo.VideoVo;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
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
                .setUsername(loginUser.getUser().getUsername())
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
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getUserId, userId);
        List<Video> videos = videoMapper.selectPage(page,wrapper).getRecords();
        if (CollectionUtils.isEmpty(videos)) {
            return Collections.emptyList();
        }
        return videos.stream().map(video -> {
            VideoVo vo = new VideoVo();
            BeanUtils.copyProperties(video, vo);
            return vo;
        }).collect(Collectors.toList());
    }
    public void refreshVisitRanking() {
        List<Video> topVideos = videoMapper.selectList(new LambdaQueryWrapper<Video>()
                .select(Video::getId, Video::getVisitCount) //只取必要的列
                .orderByDesc(Video::getVisitCount)
                .last("LIMIT 1000"));
        if (CollectionUtils.isEmpty(topVideos)) {
            return;
        }
        // 2. 准备数据：使用 Stream API 快速构建 TypedTuple 集合
        Set<ZSetOperations.TypedTuple<Object>> tuples = topVideos.stream()
                .map(v -> new DefaultTypedTuple<>((Object) v.getId(), v.getVisitCount().doubleValue()))
                .collect(Collectors.toSet());
        // 3. 使用临时 Key 刷新,防止刷新时正在写入redis
        String tempKey = VIDEO_VISIT_RANKING + ":temp";
        // 写入临时 Key 并设置过期时间
        redisTemplate.opsForZSet().add(tempKey, tuples);
        redisTemplate.expire(tempKey, 7, TimeUnit.DAYS);
        // 重命名临时 Key 为正式 Key
        // 这样在刷新的一瞬间，旧数据直接被覆盖，用户永远不会读到空数据
        redisTemplate.rename(tempKey, VIDEO_VISIT_RANKING);
    }
    @Override
    public List<VideoVo> getPopularVideo(Integer pageSize, Integer pageNum) {
        //获取排行版
        int start=(pageNum-1)*pageSize;
        int end = start + pageSize - 1;
        //  从 ZSet 获取视频 ID 列表
        Set<Object> idSet = redisTemplate.opsForZSet().reverseRange(VIDEO_VISIT_RANKING, start, end);
        //  缓存缺失处理
        if (CollectionUtils.isEmpty(idSet)) {
            // 建议：此处可以引入分布式锁，防止并发刷库
            refreshVisitRanking();
            // 刷新后重新获取一次
            idSet = redisTemplate.opsForZSet().reverseRange(VIDEO_VISIT_RANKING, start, end);
            if (CollectionUtils.isEmpty(idSet)) return Collections.emptyList();
        }
        List<Object> videoIds = idSet.stream().map(Object::toString).collect(Collectors.toList());
        // 关键优化：使用 IN 查询，一次性查出所有数据 (批量查询)
        List<Video> videos = videoMapper.selectVideosByIdList(videoIds);

        // 内存排序：selectBatchIds 不保证顺序，需按照 videoIds 的顺序重排
        //先放入map中
        Map<String, VideoVo> videoMap = videos.stream().map((video)->{
                    VideoVo vo = new VideoVo();
                    BeanUtils.copyProperties(video, vo);
                    return vo;
                })
                .collect(Collectors.toMap(VideoVo::getId, v -> v));
        //再通过videoIds流按顺序整理
        return videoIds.stream()
                .map(videoMap::get)
                .filter(Objects::nonNull) // 过滤掉数据库里可能不存在的视频
                .collect(Collectors.toList());
    }
    @Override
    public List<VideoVo> searchVideo(String keywords,Integer pageSize,Integer pageNum,String username) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getTitle, keywords)
                .eq(Video::getUsername, username);
        Search search = new Search().setKeyword(keywords);
        searchMapper.insert(search);
        List<Video> videos = videoMapper.selectPage(page,wrapper).getRecords();
        List<VideoVo> videoVos=videos.stream().map((v)->{
            VideoVo vo = new VideoVo();
            BeanUtils.copyProperties(v, vo);
            return vo;
        }).collect(Collectors.toList());
        return videoVos;
    }
}
