package work.work4.service;
import com.alibaba.fastjson.JSON;
import com.aliyun.core.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import work.work4.common.LoginUser;
import work.work4.dto.CommentDto;
import work.work4.dto.LikeDto;
import work.work4.mapper.CommentMapper;
import work.work4.mapper.LikeMapper;
import work.work4.mapper.VideoMapper;
import work.work4.pojo.Like;
import work.work4.pojo.Video;
import work.work4.service.Interface.ActionServiceInterface;
import work.work4.pojo.Comment;
import work.work4.vo.CommentVo;
import work.work4.vo.VideoVo;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ActionService implements ActionServiceInterface {
    @Resource
    private LikeMapper likeMapper;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private VideoMapper videoMapper;
    @Resource
    private StringRedisTemplate template;
    private static final String VIDEO_LIKE_COUNT_KEY = "video:like:count:";
    private static final String COMMENT_LIKE_COUNT_KEY = "comment:like:count:";
    private static final String USER_LIKE_ZSET = "user:likes:";
    private static final String COMMENT_COMMENT_COUNT_KEY = "comment:childComment:count:";
    private static final String VIDEO_COMMENT_COUNT_KEY = "video:comment:count:";
    @Override
    public void likeAction(String videoId, String commentId, String actionType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("用户未登录或认证失效");
        }
        LoginUser loginUser=(LoginUser) authentication.getPrincipal();
        String userId =loginUser.getUser().getId();
        // 1. 互斥校验
        boolean hasVideoId = !StringUtils.isEmpty(videoId);
        boolean hasCommentId = !StringUtils.isEmpty(commentId);
        String targetId = hasVideoId ? videoId : commentId;
        String countKey = hasVideoId ? VIDEO_LIKE_COUNT_KEY + targetId : COMMENT_LIKE_COUNT_KEY + targetId;
        String dirtySetKey = hasVideoId ? "sync:video:ids" : "sync:comment:ids";
        // 行为记录 Key (用于异步写入 Like 表)
        String actionQueueKey = "queue:like:actions";
        if (hasVideoId == hasCommentId) {
            throw new IllegalArgumentException("必须且只能提供一个 ID");
        }

        boolean isLike = "1".equals(actionType);

        // 2. 如果缓存不存在，先初始化（防止缓存击穿导致的计数错误）
        if (Boolean.FALSE.equals(template.hasKey(countKey))) {
            Integer dbCount = hasVideoId ?
                    videoMapper.selectById(targetId).getLikeCount() :
                    commentMapper.selectById(targetId).getLikeCount();
            template.opsForValue().set(countKey, String.valueOf(dbCount), 1, TimeUnit.DAYS);
        }
        if (isLike) {
            template.opsForValue().increment(countKey);
            // 实时维护用户点赞列表 (ZSet)，Score 用当前时间戳用于排序
            template.opsForZSet().add(USER_LIKE_ZSET + userId, targetId, System.currentTimeMillis());
        } else {
            // 获取当前值，避免减成负数
            Object val = template.opsForValue().get(countKey);
            if (val != null && Long.parseLong(val.toString()) > 0) {
                template.opsForValue().decrement(countKey);
            }
            template.opsForZSet().remove(USER_LIKE_ZSET + userId, targetId);
        }
        LikeDto likeDto = new LikeDto().setActionType(actionType);
        if (hasVideoId) {
            likeDto.setUserId(userId).setVideoId(targetId);
        }else {
            likeDto.setUserId(userId).setCommentId(targetId);;
        }

        // 3. 将“点赞行为”存入 Redis List 消息队列
        // 数据格式：userId:targetId:type:isComment
        template.opsForList().rightPush(actionQueueKey, JSON.toJSONString(likeDto));
        // 4. 将 ID 加入“待同步”集合
        template.opsForSet().add(dirtySetKey, targetId);
    }

    @Override
    public List<VideoVo> getLikeList(String userId, Integer pageSize, Integer pageNum) {
        long start = (long) (pageNum - 1) * pageSize;
        long end = start + pageSize - 1;

        // 2. 从 Redis ZSet 中获取按时间倒序的视频 ID 集合
        Set<String> videoIds = template.opsForZSet().reverseRange(USER_LIKE_ZSET + userId, start, end);


        if (CollectionUtils.isEmpty(videoIds)) {
            return new ArrayList<>();
        }
        List<Object> idList = new ArrayList<>(videoIds);
        // 4. 批量从数据库查询视频详情
        // 注意：selectBatchIds 返回的顺序不一定匹配 idList 的顺序
        List<Video> videos = videoMapper.selectVideosByIdList(idList);

        // 5. 按照 idList 的原始顺序进行排序并转换为 VO
        // (因为用户点赞列表通常要求严格按时间倒序，数据库批量查询会打乱顺序)
        Map<String, Video> videoMap = videos.stream()
                .collect(Collectors.toMap(Video::getId, v -> v));

        return idList.stream()
                .map(id -> {
                    Video video = videoMap.get(id);
                    VideoVo videoVo = new VideoVo();
                    if (video == null) return null;
                    // 转换为 VO 对象 (手动转换或使用 BeanUtils)
                    BeanUtils.copyProperties(video, videoVo);
                    return videoVo;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void comment(String videoId,String commentId,String content) {
        // 1. 互斥校验
        boolean hasVideoId = !StringUtils.isEmpty(videoId);
        boolean hasCommentId = !StringUtils.isEmpty(commentId);
        if (hasVideoId == hasCommentId) {
            System.out.println("只能有一个id");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("用户未登录或认证失效");
        }
        LoginUser loginUser=(LoginUser) authentication.getPrincipal();
        String userId =loginUser.getUser().getId();
        String countKey = hasVideoId ? VIDEO_COMMENT_COUNT_KEY + videoId : COMMENT_COMMENT_COUNT_KEY + commentId;
        String dirtySetKey = hasVideoId ? "sync:videoCommentCount:Ids" : "sync:commentChildCount:Ids";
        String targetId = hasVideoId ? videoId : commentId;
        if (Boolean.FALSE.equals(template.hasKey(countKey))) {
            Integer dbCount = hasVideoId ?
                    videoMapper.selectById(targetId).getCommentCount() :
                    commentMapper.selectById(targetId).getChildCount();
            template.opsForValue().set(countKey, String.valueOf(dbCount), 1, TimeUnit.DAYS);
        }
        template.opsForValue().increment(countKey);
        Comment comment = new Comment().setUserId(userId);
        if (hasCommentId) {
            comment.setParentId(targetId);
        }else{
            comment.setVideoId(targetId);
        }
        comment.setContent(content);
        commentMapper.insert(comment);
        // 4. 将 ID 加入“待同步”集合
        template.opsForSet().add(dirtySetKey, targetId);
    }

    @Override
    public List<CommentVo> getCommentList(String videoId,String commentId,Integer pageSize,Integer pageNum) {
        // 1. 互斥校验
        boolean hasVideoId = !StringUtils.isEmpty(videoId);
        boolean hasCommentId = !StringUtils.isEmpty(commentId);
        if (hasVideoId == hasCommentId) {
            System.out.println("只能有一个id");
        }
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(hasVideoId,Comment::getVideoId, videoId)
                .eq(hasCommentId, Comment::getParentId, commentId);
        List<CommentVo> commentVos=commentMapper.selectPage(page, wrapper).getRecords()
                .stream()
                .map(comment -> {
                    CommentVo commentVo = new CommentVo();
                    BeanUtils.copyProperties(comment, commentVo);
                    return commentVo;
                }).toList();
        return commentVos;
    }

    @Override
    public void deleteComment(String videoId,String commentId) {
        boolean hasVideoId = !StringUtils.isEmpty(videoId);
        boolean hasCommentId = !StringUtils.isEmpty(commentId);
        if (hasVideoId == hasCommentId) {
            System.out.println("只能有一个id");
        }
        System.out.println(1);
        if (hasVideoId) {
            LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Comment::getVideoId, videoId);
            commentMapper.delete(wrapper);
            template.opsForValue().getOperations().delete(VIDEO_COMMENT_COUNT_KEY + videoId);
        } else {
            LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Comment::getParentId, commentId).or()
                            .eq(Comment::getId, commentId);
            commentMapper.delete(wrapper);
            template.opsForValue().getOperations().delete(COMMENT_COMMENT_COUNT_KEY + commentId);
        }
    }
}
