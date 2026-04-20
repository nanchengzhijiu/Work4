package work.work4.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import work.work4.pojo.UserBlack;

import java.util.List;

@Mapper
public interface UserBlackMapper extends BaseMapper<UserBlack> {
    List<String> getBlackListByUserId(String userId);
}
