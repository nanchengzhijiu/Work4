package work.work4.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ChatVo {
    @JsonProperty("id")
    private String id;
    @JsonProperty("chat_name")
    private String chatName;
    @JsonProperty("create_at")
    private LocalDateTime createdAt;
    @JsonProperty("type")
    private int type;
}
