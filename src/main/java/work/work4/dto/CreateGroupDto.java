package work.work4.dto;

import lombok.Data;

import java.util.List;

@Data

public class CreateGroupDto {
    private String chatName;
    private String owenerId;
    private List<String> memberIds;
}
