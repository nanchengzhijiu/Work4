package work.work4.task;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import work.work4.mapper.CommentMapper;
import java.util.Set;

@Component
public class CommentSyncTask {
    @Resource
    private RedisTemplate<String, Object> template;
    @Resource
    private CommentMapper commentMapper;
    private static final String COMMENT_LIKE_COUNT_KEY = "comment:like:count:";
    @Scheduled(cron = "0 0/5 * * * ?")
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
}
