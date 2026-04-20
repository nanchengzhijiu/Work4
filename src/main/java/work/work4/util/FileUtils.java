package work.work4.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
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
    public String[] uploadVideo(File file) throws IOException {
        // 1. 获取文件名（File 对象使用 getName()）
        String originalName = file.getName();
        // 建议：即使是 File，也建议加个 UUID 或时间戳前缀，防止 OSS 同名覆盖
        String fileName = "video/" + System.currentTimeMillis() + "_" + originalName;

        String fileBaseUrl = "https://" + bucketVideo + "." + endpoint + "/";
        OSS ossClient = getOssClient();

        // 2. 使用 FileInputStream 包装 File 对象
        try (FileInputStream inputStream = new FileInputStream(file)) {
            // OSS 的 putObject 建议传入 ObjectMetadata 设置 Content-Type，
            // 否则 OSS 可能会将其识别为 application/octet-stream，导致浏览器无法直接播放
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("video/mp4"); // 或者根据后缀动态判断
            ossClient.putObject(bucketVideo, fileName, inputStream, metadata);
        } catch (Exception e) {
            throw new IOException("视频上传至阿里云失败", e);
        } finally {
            // 3. 注意：如果你的 getOssClient() 每次都 new 一个新实例，则需要 shutdown
            // 如果是单例 Bean，则千万不要在这里 shutdown，否则下次调用会报错
            ossClient.shutdown();
        }

        // 4. 视频基础地址
        String videoUrl = fileBaseUrl + fileName;

        // 5. 封面地址：利用 OSS 视频截帧
        // 注意：如果你的 Bucket 是私有的，这个 URL 需要加签名才能访问，否则 403
        String coverUrl = videoUrl + "?x-oss-process=video/snapshot,t_0,f_jpg,w_800,h_600,m_fast";

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
