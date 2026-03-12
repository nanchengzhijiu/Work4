package work.work4.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import work.work4.common.LoginUser;
import work.work4.mapper.FollowMapper;
import work.work4.mapper.FriendMapper;
import work.work4.mapper.UserMapper;
import work.work4.pojo.*;
import work.work4.service.Interface.SocialServiceInterface;
import work.work4.vo.FollowVo;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SocialService implements SocialServiceInterface {
    @Resource
    private FollowMapper followMapper;
    @Resource
    private FriendMapper friendMapper;
    @Resource
    private UserMapper userMapper;
    @Override
    public void followAction(String toUserId,String actionType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("用户未登录或认证失效");
        }
        LoginUser loginUser=(LoginUser) authentication.getPrincipal();
        String userId =loginUser.getUser().getId();
        Follow follow = new Follow().setUserId(userId).setToUserId(toUserId);
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getToUserId, toUserId)
                .eq(Follow::getUserId, userId);
        if(actionType.equals("0")){
            followMapper.delete(wrapper);
        }else {
            followMapper.insert(follow);
            //        判断对方是否关注自己
            LambdaQueryWrapper<Follow> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.eq(Follow::getUserId, toUserId)
                    .eq(Follow::getToUserId, userId);
            if(followMapper.selectCount(wrapper1) > 0){
//                互粉
                Friend friend1 = new Friend().setUserId(userId).setFriendId(toUserId);
                Friend friend2 = new Friend().setUserId(toUserId).setFriendId(userId);
                friendMapper.insert(friend1);
                friendMapper.insert(friend2);
            }
        }
    }
    @Override
    public List<FollowVo> getFollowList(String userId,
                                        Integer pageNum,
                                        Integer pageSize) {
        Page<Follow> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId);
        List<String> followIds=followMapper.selectPage(page, wrapper).getRecords().stream()
                .map(Follow::getToUserId)
                .collect(Collectors.toList());
        if(followIds.isEmpty()){
            return null;
        }
        LambdaQueryWrapper<User> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.in(User::getId, followIds);
        List<FollowVo> followVos=userMapper.selectList(wrapper1).stream()
                .map((user)->{
                    FollowVo followVo=new FollowVo();
                    BeanUtils.copyProperties(user,followVo);
                    return followVo;
                }).collect(Collectors.toList());
        return followVos;
    }

    @Override
    public List<FollowVo> getFanList(String userId, Integer pageNum, Integer pageSize) {
        Page<Follow> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getToUserId, userId);
//        传入userId和被关注对象Id一样的userId集合
        List<String> FanIds=followMapper.selectPage(page, wrapper).getRecords().stream()
                .map(Follow::getUserId)
                .collect(Collectors.toList());
        if(FanIds.isEmpty()){
            return null;
        }
        LambdaQueryWrapper<User> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.in(User::getId, FanIds);
        List<FollowVo> followVos= userMapper.selectList(wrapper1).stream()
                .map((user)->{
                    FollowVo followVo=new FollowVo();
                    BeanUtils.copyProperties(user,followVo);
                    return followVo;
                }).collect(Collectors.toList());
        return followVos;
    }

    @Override
    public List<FollowVo> getFriendList(Integer pageNum, Integer pageSize) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            System.out.println("认证失败");
        }
        LoginUser loginUser=(LoginUser) authentication.getPrincipal();
        String userId=loginUser.getUser().getId();
        Page<Friend> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId);
        List<String> friendIds=friendMapper.selectPage(page,wrapper).getRecords().stream()
                .map(Friend::getFriendId)
                .collect(Collectors.toList());
        if (friendIds.isEmpty()){
            return null;
        }
        LambdaQueryWrapper<User> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.in(User::getId, friendIds);
        List<FollowVo> friendVos=userMapper.selectList(wrapper1).stream()
                .map((friend)->{
                    FollowVo followVo=new FollowVo();
                    BeanUtils.copyProperties(friend,followVo);
                    return followVo;
                }).collect(Collectors.toList());
        return friendVos;
    }
}
