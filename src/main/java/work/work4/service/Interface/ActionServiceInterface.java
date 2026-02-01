package work.work4.service.Interface;

import work.work4.pojo.Comment;
import work.work4.pojo.User;

import java.util.List;

public interface ActionServiceInterface {
    public void likeAction();
    public List<User>  getLikeList();
    public void comment();
    public List<Comment> getCommentList();
    public void deleteComment();
}
