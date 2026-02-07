package work.work4.service;

import jakarta.annotation.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import work.work4.mapper.UserMapper;
import work.work4.service.Interface.UserServiceInterface;
import work.work4.pojo.User;
import work.work4.util.JwtUtils;

@Service
public class UserService implements UserServiceInterface {
    @Resource
    private UserMapper userMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Override
    public void register(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setRole("USER");
        userMapper.insert(user);
    }

    @Override
    public void login(User user) {
        //security接管，不做处理
    }

    @Override
    public User getUser(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public void uploadAvatar() {

    }
}
