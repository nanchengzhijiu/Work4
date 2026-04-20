package work.work4.ws;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfo {
    private String id;
    private String username;
    private String avatarUrl;
    private String role;
    private LocalDateTime joinAt;
}
