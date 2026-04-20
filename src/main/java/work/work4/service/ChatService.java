package work.work4.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import work.work4.common.RestBean;
import work.work4.dto.CreateGroupDto;
import work.work4.mapper.ChatMapper;
import work.work4.mapper.ChatMemberMapper;
import work.work4.mapper.MessageMapper;
import work.work4.mapper.UserBlackMapper;
import work.work4.pojo.*;
import work.work4.service.Interface.ChatServiceInterface;
import work.work4.vo.ChatVo;
import work.work4.ws.UserInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static work.work4.common.RedisConstants.BLACK_USER_KEY;
import static work.work4.common.RedisConstants.CHAT_HISTORY_KEY;

@Service
public class ChatService implements ChatServiceInterface {
    @Resource
    ChatMapper chatMapper;
    @Resource
    ChatMemberMapper chatMemberMapper;
    @Resource
    MessageMapper messageMapper;
    @Resource
    UserBlackMapper userBlackMapper;
    @Resource
    RabbitTemplate rabbitTemplate;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    public RestBean<Object> createPrivate(String userId, String toUserId){
        String relationKey = generateRelationKey(userId, toUserId);
        Chat newChat = new Chat()
                .setType(1) // 1 代表私聊
                .setRelationKey(relationKey)
                .setCreatedAt(LocalDateTime.now());
        Chat existingChat = chatMapper.selectByRelationKey(relationKey);
        if (existingChat != null) {
            ChatVo chatVo=new ChatVo();
            BeanUtils.copyProperties(existingChat,chatVo);
            return RestBean.success(chatVo); // 已经存在，直接返回已有的雪花 chatId
        }
        chatMapper.insert(newChat);
        String chatId = newChat.getId();
        // 4. 拿到 ID 后，继续处理关联表（建立 ChatMember 关系）
        ChatMember member1 = new ChatMember()
                .setChatId(chatId) // 使用刚刚拿到的生成的 ID
                .setUserId(userId)
                .setRole("member")
                .setJoinAt(LocalDateTime.now());

        ChatMember member2 = new ChatMember()
                .setChatId(chatId) // 使用刚刚拿到的生成的 ID
                .setUserId(toUserId)
                .setRole("member")
                .setJoinAt(LocalDateTime.now());
        ChatVo chatVo=new ChatVo();
        BeanUtils.copyProperties(newChat,chatVo);
        chatMemberMapper.insert(member1);
        chatMemberMapper.insert(member2);
        return RestBean.success(chatVo);
    }
    @Transactional(rollbackFor = Exception.class)
    public RestBean<Object> createGroup(CreateGroupDto createGroupDto){
        Chat groupChat = new Chat()
                .setChatName(createGroupDto.getChatName())
                .setType(2) // 2 代表群聊
                .setCreatedAt(LocalDateTime.now());

        chatMapper.insert(groupChat);
        String chatId = groupChat.getId(); // 获取雪花算法生成的 ID

        // 2. 准备成员列表
        List<ChatMember> members = new ArrayList<>();

        members.add(
                new ChatMember()
                        .setId(IdWorker.getIdStr()) // 【新增】手动生成并设置雪花 ID
                        .setChatId(chatId)
                        .setUserId(createGroupDto.getOwenerId())
                        .setRole("owner")
                        .setJoinAt(LocalDateTime.now()));

// 批量加入其他成员
        for (String userId : createGroupDto.getMemberIds()) {
            members.add(new ChatMember()
                    .setId(IdWorker.getIdStr()) // 【新增】手动生成并设置雪花 ID
                    .setChatId(chatId)
                    .setUserId(userId)
                    .setRole("member")
                    .setJoinAt(LocalDateTime.now()));
            chatMemberMapper.saveByMemberIds(members);
        }
        return RestBean.success(groupChat);
    }
    public RestBean<Object> addBlack(String userId, String blackId){
        UserBlack userBlack=new UserBlack().setUserId(userId).setBlackId(blackId);
        userBlackMapper.insert(userBlack);
        return RestBean.success();
    }
    public List<String> getBlack(String userId){
        String redisKey=BLACK_USER_KEY+userId;
        List<String> blackList=stringRedisTemplate.opsForList().range(redisKey,0,-1);
//        redis查数据
        if (blackList!=null && !blackList.isEmpty()){
            System.out.println("命中redis");
            return blackList;
        }else {
            System.out.println("命中mysql");
//            redis未查到数据
            blackList=userBlackMapper.getBlackListByUserId(userId);
            if (blackList==null || blackList.isEmpty()){
//                mysql未查到，设置空值，防止缓存穿透
                stringRedisTemplate.opsForList().rightPush(redisKey,"");
                stringRedisTemplate.expire(redisKey,2,TimeUnit.MINUTES);
                return null;
            }else {
                for(String black:blackList){
                    stringRedisTemplate.opsForList().rightPush(redisKey,black);
                }
                return blackList;
            }
        }
    }
    public RestBean<Object> getChatList(String userId){
        List<Chat> chatList= chatMemberMapper.selectChatByUserId(userId);
        return RestBean.success(chatList);
    }
    public RestBean<Object> getChatUsers(String chatId){
        List<UserInfo> userList=chatMemberMapper.selectUserByChatId(chatId);
        return RestBean.success(userList);
    }
    public RestBean<Object> getMessage(String chatId,String userId){
        String redisKey=CHAT_HISTORY_KEY+chatId;
        List<String> messages = stringRedisTemplate.opsForList().range(redisKey, 0, -1);
//           redis查到数据
        if (messages != null && !messages.isEmpty()) {
            List<Messages> megs=new ArrayList<>();
            for (String msg : messages) {
                Messages message = JSON.parseObject(msg, Messages.class);
                megs.add(message);
            }
            return RestBean.success(megs);
        }else {
//            redis未查到数据
             List<Messages> megs=messageMapper.selectByChatId(chatId,userId);
             if (megs==null || megs.isEmpty()){
                 stringRedisTemplate.opsForList().rightPush(redisKey,"");
                 stringRedisTemplate.expire(redisKey,2,TimeUnit.MINUTES);
                 return RestBean.success(null);
             }
            for (Messages meg : megs) {
                    String megJson = JSON.toJSONString(meg);
                    stringRedisTemplate.opsForList().rightPush(redisKey, megJson);
            }
            return RestBean.success(megs);
        }
    }
    private String generateRelationKey(String userId1, String userId2) {
        // 按照字典序比较大小，保证小的在前面，大的在后面
        if (userId1.compareTo(userId2) < 0) {
            return "PRIVATE_" + userId1 + "_" + userId2;
        } else {
            return "PRIVATE_" + userId2 + "_" + userId1;
        }
    }
}
