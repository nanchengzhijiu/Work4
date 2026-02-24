package work.work4.util;

import javax.imageio.ImageIO;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import java.awt.image.BufferedImage;
import java.io.File;
public class VideoMetadataUtil {

    /**
     * 获取视频时长并截取第一帧
     * * @param videoPath     视频文件的绝对路径 (例如: /opt/video/test.mp4)
     * @param coverSavePath 封面图存储的绝对路径 (例如: /opt/cover/test.jpg)
     * @return 视频时长（秒），如果处理失败则返回 0
     */
    public static long processVideo(String videoPath, String coverSavePath) {
        // 使用 try-with-resources 自动关闭 grabber 资源
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {
            grabber.start();

            // 1. 获取时长 (微秒 -> 秒)
            long duration = grabber.getLengthInTime() / 1000000;

            // 2. 截取第一帧
            Frame frame = null;
            int i = 0;
            int length = grabber.getLengthInFrames();
            while (i < length) {
                frame = grabber.grabImage();
                // 过滤掉空帧或非图像帧（比如只有音频的帧）
                if (frame != null && frame.image != null) {
                    break;
                }
                i++;
            }

            // 3. 保存封面
            if (frame != null) {
                saveFrameAsJpg(frame, coverSavePath);
            }

            grabber.stop();
            return duration;

        } catch (Exception e) {
            System.out.println("视频处理工具类解析失败，路径: {}，错误: {}"+videoPath+e.getMessage());
            return 0;
        }
    }

    private static void saveFrameAsJpg(Frame frame, String outPath) throws Exception {
        Java2DFrameConverter converter = new Java2DFrameConverter();
        BufferedImage bi = converter.getBufferedImage(frame);
        File output = new File(outPath);
        ImageIO.write(bi, "jpg", output);
    }
}