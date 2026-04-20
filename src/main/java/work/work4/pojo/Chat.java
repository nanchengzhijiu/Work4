package work.work4.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("chat")
public class Chat {
    private String id;
    private String chatName;
    private int type;
    private String relationKey;
    private LocalDateTime createdAt;

}
