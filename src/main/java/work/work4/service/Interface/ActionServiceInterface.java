package work.work4.service.Interface;
import work.work4.vo.CommentVo;
import work.work4.vo.VideoVo;

import java.util.List;

public interface ActionServiceInterface {
    void likeAction(String videoId, String commentId, String actionType);
    List<VideoVo>  getLikeList(String userId, Integer pageSize, Integer pageNum);
    void comment(String videoId,String commentId,String content);
    List<CommentVo> getCommentList(String videoId, String commentId, Integer pageSize, Integer pageNum);
    void deleteComment(String videoId,String commentId);
}
