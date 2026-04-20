package work.work4.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import work.work4.pojo.Comment;
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    @Update("update comment set like_count=#{likeCount} where id=#{commentId}")
    void updateLikeCount(@Param("commentId") String commentId,@Param("likeCount") int likeCount);
    @Update("update comment set child_count=#{commentCount} where id=#{commentId}")
    void updateCommentCount(@Param("commentId") String commentId,@Param("commentCount") int commentCount);
}
