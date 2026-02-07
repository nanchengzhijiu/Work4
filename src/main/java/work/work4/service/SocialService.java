package work.work4.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import work.work4.mapper.FollowMapper;
import work.work4.mapper.FriendMapper;
import work.work4.pojo.*;
import work.work4.service.Interface.SocialServiceInterface;

import java.util.List;
@Service
public class SocialService implements SocialServiceInterface {
    @Resource
    private FollowMapper followMapper;
    @Resource
    private FriendMapper friendMapper;
    @Override
    public void followAction(Long userId,Long followId,String actionType) {
        Follow follow = new Follow().setUserId(userId).setFollowId(followId);
        followMapper.insert(follow);
    }

    @Override
    public List<Follow> getFollowList(Long userId,
                                    Integer pageNum,
                                    Integer pageSize) {
        Page<Follow> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return followMapper.selectPage(page, wrapper).getRecords();
    }

    @Override
    public List<Follow> getFanList(Long userId, Integer pageNum, Integer pageSize) {
        Page<Follow> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("follow_id", userId);
        return followMapper.selectPage(page, wrapper).getRecords();
    }

    @Override
    public List<Friend> getFriendList(Long userId,Integer pageNum, Integer pageSize) {
        Page<Friend> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return friendMapper.selectPage(page,wrapper).getRecords();
    }
}
