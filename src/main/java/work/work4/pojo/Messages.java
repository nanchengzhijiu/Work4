package work.work4.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("message")
public class Messages {
    private String id;
    private String chatId;
    private String fromUserId;
    private String content;
    private long seq;
    private int isRead;
    private String type;
    private LocalDateTime publishTime;
}
