package work.work4.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MessageDto {
    private String chatId;
    private String fromName;
    private String content;
    private long seq;
    private String type;
    private String timeStamp;
}
