package work.work4.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import work.work4.pojo.Video;

import java.util.List;

@Mapper
public interface VideoMapper extends BaseMapper<Video> {
    List<Video> selectVideosByIdList(List<Object> idList);
}
