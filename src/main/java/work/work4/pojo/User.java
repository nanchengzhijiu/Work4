package work.work4.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalTime;

@Data
@TableName("user")
public class User {
    private Long id;
    private String username;
    private String password;
    private String avatarUrl;
    private LocalTime createdAt;
    private LocalTime updatedAt;
    private LocalTime deletedAt;
}