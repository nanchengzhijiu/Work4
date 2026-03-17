package work.work4.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import work.work4.pojo.Video;

import java.util.List;

@Mapper
public interface VideoMapper extends BaseMapper<Video> {
    List<Video> selectVideosByIdList(List<String> idList);
    @Select("update video set like_count = #{likeCount} where id=#{videoId}")
    void updateLikeCount(String videoId, int likeCount);
    @Select("update video set comment_count = #{commentCount} where id=#{videoId}")
    void updateCommentCount(String videoId, int commentCount);
}
