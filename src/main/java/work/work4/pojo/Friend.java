package work.work4.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("friends")
public class Friend {
    private Long id;
    private Long userId;
    private Long friendId;
}
