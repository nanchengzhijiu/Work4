package work.work4.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import work.work4.pojo.Chat;
import work.work4.pojo.ChatMember;
import work.work4.ws.UserInfo;

import java.util.List;

@Mapper
public interface ChatMemberMapper extends BaseMapper<ChatMember> {
    List<Chat> selectChatByUserId(@Param("userId") String userId);
    void saveByMemberIds(@Param("members") List<ChatMember> members);
    List<UserInfo> selectUserByChatId(@Param("chatId")String chatId);
}
