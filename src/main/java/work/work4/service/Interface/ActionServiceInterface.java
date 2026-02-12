package work.work4.service.Interface;

import work.work4.dto.CommentDto;
import work.work4.dto.LikeDto;
import work.work4.pojo.Comment;
import work.work4.pojo.Video;

import java.util.List;

public interface ActionServiceInterface {
    void likeAction(LikeDto likeDto);
    List<Video>  getLikeList(Long userId,Integer pageSize,Integer pageNum);
    void comment(CommentDto commentDto);
    List<Comment> getCommentList(Long videoId,Long commentId,Integer pageSize,Integer pageNum);
    void deleteComment(CommentDto commentDto);
}
