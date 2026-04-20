package work.work4.ws;

import lombok.Data;

import java.util.List;

@Data
public class ResultMessage {
    private String fromName;
    private List<String> toUserIds;
    private String message;
}
