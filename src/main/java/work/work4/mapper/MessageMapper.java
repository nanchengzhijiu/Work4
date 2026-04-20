package work.work4.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.bind.annotation.RequestParam;
import work.work4.pojo.Messages;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Messages> {
    List<Messages> selectByChatId(@RequestParam("chatId") String chatId, @RequestParam("userId") String userId);
}
