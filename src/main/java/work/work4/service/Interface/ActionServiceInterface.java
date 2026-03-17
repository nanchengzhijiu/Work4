package work.work4.service.Interface;
import work.work4.common.RestBean;
import work.work4.vo.CommentVo;
import work.work4.vo.VideoVo;

import java.util.List;

public interface ActionServiceInterface {
    void likeAction(String videoId, String commentId, String actionType);
    RestBean<Object> getLikeList(String userId, Integer pageSize, Integer pageNum);
    void comment(String videoId,String commentId,String content);
    RestBean<Object> getCommentList(String videoId, String commentId, Integer pageSize, Integer pageNum);
    void deleteComment(String videoId,String commentId);
}
