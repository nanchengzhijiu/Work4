package work.work4.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import work.work4.common.RestBean;
import work.work4.dto.CreateGroupDto;
import work.work4.service.ChatService;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Resource
    private ChatService chatService;
    @PostMapping("/private")
    public RestBean<Object> createPrivate(@RequestParam("userId") String userId
            , @RequestParam("toUserId") String toUserId){
        return chatService.createPrivate(userId,toUserId);
    }
    @PostMapping("/group")
    public RestBean<Object> createGroup(@RequestBody CreateGroupDto createGroupDto){
        return chatService.createGroup(createGroupDto);
    }
    @PostMapping("/addBlack")
    public RestBean<Object> addBlack(@RequestParam String userId,@RequestParam String blackId){
        return chatService.addBlack(userId,blackId);
    }
    @GetMapping("/getBlackList")
    public RestBean<Object> getBlackList(@RequestParam String userId){
        return RestBean.success(chatService.getBlack(userId));
    }
    @GetMapping("/getChatList")
    public RestBean<Object> getChatList(@RequestParam("userId") String userId){
        return chatService.getChatList(userId);
    }
    @GetMapping("/getChatUser")
    public RestBean<Object> getChatUsers(@RequestParam("chatId") String chatId){
        return chatService.getChatUsers(chatId);
    }
    @GetMapping("/getMessage")
    public RestBean<Object> getMessage(@RequestParam("chatId") String chatId,@RequestParam("userId") String userId){
        return chatService.getMessage(chatId,userId);
    }
}
