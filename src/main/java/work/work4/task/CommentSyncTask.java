package work.work4.task;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import io.lettuce.core.ScriptOutputType;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import work.work4.mapper.CommentMapper;
import work.work4.mapper.VideoMapper;

import java.util.Set;

import static work.work4.common.RedisConstants.*;

@Component
public class CommentSyncTask {
    @Resource
    private RedisTemplate<String, Object> template;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private VideoMapper videoMapper;
    @Scheduled(cron = "0/30 * * * * ?")
    public void syncCommentLikeCount() {
        String dirtySetKey="sync:comment:ids";
        Set<Object> commentIds=template.opsForSet().members(dirtySetKey);
        if (CollectionUtils.isEmpty(commentIds)) return;
        for (Object idObj : commentIds) {
            String commentId=String.valueOf(idObj);
            String countKey = COMMENT_LIKE_COUNT_KEY + commentId;
            Object count = template.opsForValue().get(countKey);
            if (count != null) {
                commentMapper.updateLikeCount(commentId, Integer.parseInt(count.toString()));
                template.opsForSet().remove(dirtySetKey, commentId);
            }
        }
    }
    @Scheduled(cron = "0/30 * * * * ?")
    public void syncCommentChildCount() {
        String dirtySetKey="sync:commentChildCount:Ids";
        Set<Object> commentIds=template.opsForSet().members(dirtySetKey);
        if (CollectionUtils.isEmpty(commentIds)) return;
        for (Object idObj : commentIds) {
            String commentId=String.valueOf(idObj);
            String countKey = COMMENT_COMMENT_COUNT_KEY + commentId;
            Object count = template.opsForValue().get(countKey);
            if (count != null) {
                commentMapper.updateCommentCount(commentId, Integer.parseInt(count.toString()));
                template.opsForSet().remove(dirtySetKey, commentId);
            }
        }
    }
    @Scheduled(cron = "0/30 * * * * ?")
    public void syncVideoCommentCount() {
        String dirtySetKey="sync:videoCommentCount:Ids";
        Set<Object> videoIds=template.opsForSet().members(dirtySetKey);
        if (CollectionUtils.isEmpty(videoIds)) return;
        for (Object idObj : videoIds) {
            String videoId=String.valueOf(idObj);
            String countKey = VIDEO_COMMENT_COUNT_KEY + videoId;
            Object count = template.opsForValue().get(countKey);
            if (count != null) {
                videoMapper.updateCommentCount(videoId, Integer.parseInt(count.toString()));
                template.opsForSet().remove(dirtySetKey, videoId);
            }
        }
    }
}
