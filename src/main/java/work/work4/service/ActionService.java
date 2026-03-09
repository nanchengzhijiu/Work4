package work.work4.service;
import com.aliyun.core.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import work.work4.common.LoginUser;
import work.work4.dto.CommentDto;
import work.work4.mapper.CommentMapper;
import work.work4.mapper.LikeMapper;
import work.work4.mapper.VideoMapper;
import work.work4.pojo.Video;
import work.work4.service.Interface.ActionServiceInterface;
import work.work4.pojo.Comment;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
        // 3. 将“点赞行为”存入 Redis List 消息队列
        // 数据格式：userId:targetId:type:isComment
        String actionData = String.format("%s:%s:%s:%b",
                userId, targetId, actionType, !hasVideoId);
        template.opsForList().rightPush(actionQueueKey, actionData);
        // 4. 将 ID 加入“待同步”集合
        template.opsForSet().add(dirtySetKey, targetId);
    }

    @Override
    public List<Video> getLikeList(String userId,Integer pageSize,Integer pageNum) {
        long start = (pageNum - 1) * pageSize;
        long end = start + pageSize - 1;
        // 返回按时间倒序排列的 targetId 集合
        template.opsForZSet().reverseRange(USER_LIKE_ZSET + userId, start, end);
        return null;
    }

    @Override
    public void comment(CommentDto commentDto) {
        if(commentDto.getCommentId()==null && commentDto.getVideoId()==null){
            return;
        }
        Comment comment = new Comment();
        if (commentDto.getCommentId() != null) {
            comment.setCommentId(commentDto.getCommentId());
        }else{
            comment.setVideoId(commentDto.getVideoId());
        }
        comment.setContent(commentDto.getCotent());
        commentMapper.insert(comment);
    }

    @Override
    public List<Comment> getCommentList(String videoId,String commentId,Integer pageSize,Integer pageNum) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq(videoId!=null,"video_id", videoId)
                .eq(commentId!=null, "comment_id", commentId);
        return commentMapper.selectPage(page, wrapper).getRecords();
    }

    @Override
    public void deleteComment(CommentDto commentDto) {
        commentMapper.deleteById(commentDto.getCommentId());
    }
}
