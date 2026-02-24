package work.work4.service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import work.work4.dto.CommentDto;
import work.work4.dto.LikeDto;
import work.work4.mapper.CommentMapper;
import work.work4.mapper.LikeMapper;
import work.work4.mapper.VideoMapper;
import work.work4.pojo.Like;
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
    public void likeAction(LikeDto likeDto) {
        Like like = new Like().setCommentId(likeDto.getCommentId()).setVideoId(likeDto.getVideoId());
        QueryWrapper<Like> wrapper = new QueryWrapper<>();
        wrapper.eq(like.getVideoId() != 0, "video_id", like.getVideoId())
                .eq(like.getCommentId() != 0, "comment_id", like.getCommentId());
        // 更新点赞数
        if (likeDto.getActionType()==1) {
            like.setTotal(like.getTotal() + 1);
        } else if (like.getTotal() > 0) {
            like.setTotal(like.getTotal() - 1);
        }
        String countKey=VIDEO_LIKE_COUNT_KEY+like.getVideoId();
        // 如果Redis中已存在该key，进行增减操作
        if (Boolean.TRUE.equals(template.hasKey(countKey))) {
            if (likeDto.getActionType()==1) {
                template.opsForValue().increment(countKey);
            } else if (like.getTotal() > 0)  {
                template.opsForValue().decrement(countKey);
            }
        } else {
            // Redis中没有，从MySQL查询后设置
            Video video = videoMapper.selectById(likeDto.getVideoId());
            if (video != null) {
               template.opsForValue().set(countKey, video.getLikeCount().toString(), 1, TimeUnit.DAYS);
            }
        }
        // 执行更新
        likeMapper.update(like, wrapper);
    }

    @Override
    public List<Video> getLikeList(Long userId,Integer pageSize,Integer pageNum) {
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
        Comment comment = new Comment();
        if (commentDto.getCommentId() != 0) {
            comment.setCommentId(commentDto.getCommentId());
        }
        if (commentDto.getVideoId() != 0) {
            comment.setVideoId(commentDto.getVideoId());
        }
        comment.setContent(commentDto.getCotent());
        commentMapper.insert(comment);
    }

    @Override
    public List<Comment> getCommentList(Long videoId,Long commentId,Integer pageSize,Integer pageNum) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq(videoId!=0,"video_id", videoId)
                .eq(commentId!=0, "comment_id", commentId);
        return commentMapper.selectPage(page, wrapper).getRecords();
    }

    @Override
    public void deleteComment(CommentDto commentDto) {
        commentMapper.deleteById(commentDto.getCommentId());
    }
}
