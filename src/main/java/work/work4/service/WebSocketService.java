package work.work4.service;

import com.alibaba.fastjson.JSON;

import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import work.work4.config.GetTokenConfig;
import work.work4.config.RabbitConfiguration;
import work.work4.pojo.Messages;
import work.work4.util.JwtUtils;
import work.work4.ws.ResultMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value="/ws",configurator = GetTokenConfig.class) // 暴露的 WebSocket 端点路径
@Component
public class WebSocketService {
    // 存放所有在线的客户端 Session
    private static final ConcurrentHashMap<String, Session> onlineSessions = new ConcurrentHashMap<>();
    private static RabbitTemplate rabbitTemplate;
    // 引入一个线程池专门负责发消息
    private static ThreadPoolTaskExecutor pushExecutor;
    private static ChatService chatService;
    @Resource
    private void setRabbitTemplate(RabbitTemplate rabbitTemplate){
        WebSocketService.rabbitTemplate = rabbitTemplate;

    }


    @Resource
    public void setChatService(ChatService chatService) {
        WebSocketService.chatService = chatService;
    }
    @Resource
    private void setThreadPoolTaskExecutor( ThreadPoolTaskExecutor pushExecutor){
        WebSocketService.pushExecutor = pushExecutor;
    }
    @OnOpen
    public void onOpen(Session session,EndpointConfig config) {
        String token=(String) config.getUserProperties().get("token");
        String userId=JwtUtils.resolveJwt(token).getUser().getId();
        session.getUserProperties().put("userId",userId);
        onlineSessions.put(userId,session);

    }
    @OnMessage
    public void onMessage(String message, Session session) {
        String userId=session.getUserProperties().get("userId").toString();
        ResultMessage msg= JSON.parseObject(message, ResultMessage.class);
        List<String> toUserIds=msg.getToUserIds();
        Messages msg1=JSON.parseObject(message,Messages.class);
        msg1.setSeq(1).setIsRead(0).setPublishTime(LocalDateTime.now());
        System.out.println("收到用户 " + userId+"对"+ toUserIds + " 的消息: " + message);
        rabbitTemplate.convertAndSend(
                RabbitConfiguration.CHAT_EXCHANGE,
                RabbitConfiguration.CHAT_ROUTING_KEY,
                msg1);
        pushExecutor.execute(() -> pushMessageToLocal(msg1, toUserIds,userId));
    }

    @OnClose
    public void onClose(Session session) {
        // 用户断开连接时，从 Map 中移除
        String userId=session.getUserProperties().get("userId").toString();
        onlineSessions.remove(userId);
        System.out.println("用户"+userId+"断开, 当前人数: "+onlineSessions.size());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    private static void pushMessageToLocal(Messages message, List<String> toUserIds,String currentUserId){
        for (String toUserId : toUserIds){
            Session targetSession = onlineSessions.get(toUserId);
//            用户不在线或为自己
            if (targetSession == null || toUserId.equals(currentUserId)){
                continue;
            }
            if(isTargetBlack(message.getFromUserId(), toUserId)){
                System.out.println("用户id为"+toUserId+"将你划入黑名单中");
                continue;
            }
            // 使用异步发送，防止发送大文件时阻塞
            targetSession.getAsyncRemote().sendText(message.getContent());
        }
    }
    private static boolean isTargetBlack(String userId,String targetId){
        List<String> targetUserBlack = chatService.getBlack(targetId);
        return targetUserBlack.contains(userId);
    }
}
