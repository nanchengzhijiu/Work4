package work.work4.service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import work.work4.mapper.CommentMapper;
import work.work4.mapper.LikeMapper;
import work.work4.mapper.VideoMapper;
import work.work4.pojo.Like;
import work.work4.pojo.Video;
import work.work4.service.Interface.ActionServiceInterface;
import work.work4.pojo.Comment;

import java.util.List;
@Service
public class ActionService implements ActionServiceInterface {
    @Resource
    private LikeMapper likeMapper;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private VideoMapper videoMapper;
    @Override
    public void likeAction(Like like,String actionType) {
        QueryWrapper<Like> wrapper = new QueryWrapper<>();
        wrapper.eq(like.getVideoId() != null, "video_id", like.getVideoId())
                .eq(like.getCommentId() != null, "comment_id", like.getCommentId());
        // 更新点赞数
        if ("1".equals(actionType)) {
            like.setTotal(like.getTotal() + 1);
        } else if (like.getTotal() > 0) {
            like.setTotal(like.getTotal() - 1);
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
    public void comment(Comment comment) {
        commentMapper.insert(comment);
    }

    @Override
    public List<Comment> getCommentList(Long videoId,Integer pageSize,Integer pageNum) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("video_id", videoId);
        return commentMapper.selectPage(page, wrapper).getRecords();
    }

    @Override
    public void deleteComment(Comment comment) {
        commentMapper.deleteById(comment.getId());
    }
}
