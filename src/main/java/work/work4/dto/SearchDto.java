package work.work4.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SearchDto {
    private String keyword;
    private Integer pageSize;
    private Integer pageNum;
    private LocalDateTime publishTime;
    private String type;
    private String year;
}
