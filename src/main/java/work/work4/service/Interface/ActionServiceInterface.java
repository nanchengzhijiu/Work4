package work.work4.service.Interface;

import work.work4.pojo.Comment;

import java.util.List;

public interface ActionServiceInterface {
    public void like();
    public void getLikeList();
    public void comment();
    public List<Comment> getCommentList();
    public void deleteComment();
}
