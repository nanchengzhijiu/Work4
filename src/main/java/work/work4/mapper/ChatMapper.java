package work.work4.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import work.work4.pojo.Chat;

@Mapper
public interface ChatMapper extends BaseMapper<Chat> {
    @Select("SELECT * FROM chat WHERE relation_key = #{relationKey}")
    Chat selectByRelationKey(@Param("relationKey")String relationKey);
}
