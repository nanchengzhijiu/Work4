package work.work4.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import work.work4.pojo.Comment;
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    @Select("update comment set like_count=#{likeCount} where id=#{commentId}")
    void updateLikeCount(String commentId, int likeCount);
}
