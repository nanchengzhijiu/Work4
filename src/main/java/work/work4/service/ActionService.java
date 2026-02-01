package work.work4.service;

import org.springframework.stereotype.Service;
import work.work4.service.Interface.ActionServiceInterface;
import work.work4.pojo.Comment;

import java.util.List;
@Service
public class ActionService implements ActionServiceInterface {
    @Override
    public void like() {

    }

    @Override
    public void getLikeList() {

    }

    @Override
    public void comment() {

    }

    @Override
    public List<Comment> getCommentList() {
        return List.of();
    }

    @Override
    public void deleteComment() {

    }
}
