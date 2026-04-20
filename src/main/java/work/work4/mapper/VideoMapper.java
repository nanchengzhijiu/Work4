package work.work4.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import work.work4.pojo.Video;

import java.util.List;

@Mapper
public interface VideoMapper extends BaseMapper<Video> {
    List<Video> selectVideosByIdList(List<String> idList);
    @Update("update video set like_count = #{likeCount} where id=#{videoId}")
    void updateLikeCount(@Param("videoId") String videoId, @Param("likeCount") int likeCount);
    @Update("update video set comment_count = #{commentCount} where id=#{videoId}")
    void updateCommentCount(@Param("videoId") String videoId, @Param("commentCount") int commentCount);
}
