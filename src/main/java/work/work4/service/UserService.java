package work.work4.service;

import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import work.work4.mapper.UserMapper;
import work.work4.service.Interface.UserServiceInterface;
import work.work4.pojo.User;
import work.work4.util.JwtUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class UserService implements UserServiceInterface {
    @Resource
    private UserMapper userMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Override
    public void register(User user) {
//        加密注册
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setRole("USER");
        userMapper.insert(user);
    }

//    @Override
//    public void login(User user) {
//        //security不处理了
//    }

    @Override
    public User getUser(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public void uploadAvatar(MultipartFile file) throws IOException{
        String pathName="static/avatar";
        File directory = new File(pathName);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new IOException("无法创建目录: " + directory);
            }
        }
        //上传文件名
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))+file.getOriginalFilename();
        // 构建目标文件路径
        Path targetLocation = Paths.get(pathName).resolve(fileName);
        // 保存文件
        try (InputStream inputStream = file.getInputStream()) {
        //覆盖相同文件
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
