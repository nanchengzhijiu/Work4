package work.work4.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Component
public class FileUtils {
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;
    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;
    @Value("${aliyun.oss.bucket-avatar}")
    private String bucketAvatar;
    @Value("${aliyun.oss.bucket-video}")
    private String bucketVideo;
    private String fileBaseUrl="https://";
    private OSS getOssClient() {
        return new OSSClientBuilder().build("http://"+endpoint, accessKeyId, accessKeySecret);
    }
    public String uploadPicture(MultipartFile multipartFile) throws IOException {
        InputStream inputStream = multipartFile.getInputStream();
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))+multipartFile.getOriginalFilename();
        OSS ossClient = getOssClient();
        ossClient.putObject(bucketAvatar, fileName, inputStream);
        String fileBaseUrl="https://"+bucketAvatar+"."+endpoint+"/";
        String url = fileBaseUrl+fileName;
        ossClient.shutdown();
        return url;
    }
    public String[] uploadVideo(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        // 使用 UUID + 时间戳防止文件名重复
        String fileName = "video/" + originalName;
        String fileBaseUrl="https://"+bucketVideo+"."+endpoint+"/";
        OSS ossClient = getOssClient();
        try {
            ossClient.putObject(bucketVideo, fileName, file.getInputStream());
        } finally {
            ossClient.shutdown();
        }

        // 视频基础地址
        String videoUrl = fileBaseUrl+fileName;
        // 关键点：利用 OSS 视频截帧处理参数
        // t_0 表示第 0 毫秒（第一帧），f_jpg 表示格式
        String coverUrl = fileBaseUrl+fileName + "?x-oss-process=video/snapshot,t_0,f_jpg,w_800,h_600,m_fast";

        return new String[]{videoUrl, coverUrl};
    }

    public void deleteFile(String bucketName, String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) return;

        OSS ossClient = getOssClient();
        try {
            // 检查文件是否存在，存在再删除
            boolean found = ossClient.doesObjectExist(bucketName, objectKey);
            if (found) {
                ossClient.deleteObject(bucketName, objectKey);
            }
        } finally {
            ossClient.shutdown();
        }
    }
}
