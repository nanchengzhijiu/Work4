package work.work4.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
@Data
public class VideoUploadDto {
    private MultipartFile videoFile;
    private String title;
    private String description;
}
