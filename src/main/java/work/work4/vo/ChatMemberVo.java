package work.work4.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ChatMemberVo {
    @JsonProperty("id")
    private String id;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("chat_id")
    private String chatId;
    @JsonProperty("role")
    private String role;
    @JsonProperty("join_at")
    private LocalDateTime joinAt;
}
