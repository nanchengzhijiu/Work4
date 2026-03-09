package work.work4.service.Interface;

import work.work4.dto.CommentDto;
import work.work4.pojo.Comment;
import work.work4.pojo.Video;

import java.util.List;

public interface ActionServiceInterface {
    void likeAction(String videoId, String commentId, String actionType);
    List<Video>  getLikeList(String userId,Integer pageSize,Integer pageNum);
    void comment(CommentDto commentDto);
    List<Comment> getCommentList(String videoId,String commentId,Integer pageSize,Integer pageNum);
    void deleteComment(CommentDto commentDto);
}
