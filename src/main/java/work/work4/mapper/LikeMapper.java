package work.work4.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import work.work4.pojo.Like;
import work.work4.pojo.Video;

import java.util.List;

@Mapper
public interface LikeMapper extends BaseMapper<Like> {
    @Select("select video_id from `like` where user_id=#{userId}")
    List<String> selectByUserId(String userId);
}
