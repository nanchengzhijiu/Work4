package work.work4.service;
import com.aliyun.core.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
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
    @Override
    public void likeAction(String videoId, String commentId, String actionType) {
        // 1. 互斥校验
        boolean hasVideoId = !StringUtils.isEmpty(videoId);
        boolean hasCommentId = !StringUtils.isEmpty(commentId);
        if (hasVideoId == hasCommentId) {
            throw new IllegalArgumentException("必须且只能提供一个 ID");
        }

        String id = hasVideoId ? videoId : commentId;
        String countKey = hasVideoId ? VIDEO_LIKE_COUNT_KEY + id : COMMENT_LIKE_COUNT_KEY + id;
        // 用于记录哪些 ID 的点赞数发生了变化
        String dirtySetKey = hasVideoId ? "sync:video:ids" : "sync:comment:ids";

        boolean isLike = "1".equals(actionType);

        // 2. 如果缓存不存在，先初始化（防止缓存击穿导致的计数错误）
        if (Boolean.FALSE.equals(template.hasKey(countKey))) {
            Integer dbCount = hasVideoId ?
                    videoMapper.selectById(id).getLikeCount() :
                    commentMapper.selectById(id).getLikeCount();
            template.opsForValue().set(countKey, String.valueOf(dbCount), 1, TimeUnit.DAYS);
        }

        // 3. 执行 Redis 原子增减
        if (isLike) {
            template.opsForValue().increment(countKey);
        } else {
            // 获取当前值，避免减成负数
            Object val = template.opsForValue().get(countKey);
            if (val != null && Long.parseLong(val.toString()) > 0) {
                template.opsForValue().decrement(countKey);
            }
        }

        // 4. 将 ID 加入“待同步”集合
        template.opsForSet().add(dirtySetKey, id);
    }

    @Override
    public List<Video> getLikeList(String userId,Integer pageSize,Integer pageNum) {
        // 分页设置
        Page<Video> page = new Page<>(pageNum, pageSize);

        // 创建查询条件
        QueryWrapper<Video> wrapper = new QueryWrapper<>();

        // 子查询：从like表获取该用户点赞的video_id
        wrapper.inSql("id",
                "SELECT video_id FROM like WHERE user_id = " + userId + " AND video_id IS NOT NULL"
        );
        // 执行查询
        return videoMapper.selectPage(page, wrapper).getRecords();
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
