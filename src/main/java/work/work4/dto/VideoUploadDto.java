package work.work4.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
@Data
public class VideoUploadDto {
    private MultipartFile videoFile;
    private Long userId;
    private String title;
    private String description;
    private String type;
    private String year;
}
