package work.work4.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("chat_member")
public class ChatMember {
    private String id;
    private String userId;
    private String chatId;
    private String role;
    private LocalDateTime joinAt;
}
