package work.work4.service.Interface;

import work.work4.pojo.Comment;
import work.work4.pojo.Like;
import work.work4.pojo.Video;

import java.util.List;

public interface ActionServiceInterface {
    void likeAction(Like like,String actionType);
    List<Video>  getLikeList(Long userId,Integer pageSize,Integer pageNum);
    void comment(Comment comment);
    List<Comment> getCommentList(Long videoId,Integer pageSize,Integer pageNum);
    void deleteComment(Comment comment);
}
