package work.work4.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MessageVo {
    @JsonProperty("id")
    private String id;
    @JsonProperty("chat_id")
    private String chatId;
    @JsonProperty("from_name")
    private String fromName;
    @JsonProperty("to_name")
    private String toName;
    @JsonProperty("content")
    private String content;
    @JsonProperty("seq")
    private long seq;
    @JsonProperty("type")
    private String type;
    @JsonProperty("timeStamp")
    private String timeStamp;
}
