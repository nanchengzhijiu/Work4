package work.work4.service;
import cn.hutool.core.bean.BeanUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import work.work4.common.LoginUser;
import work.work4.common.RestBean;
import work.work4.mapper.UserMapper;
import work.work4.pojo.User;
import work.work4.service.Interface.UserServiceInterface;
import work.work4.util.FileUtils;
import work.work4.util.JwtUtils;
import work.work4.util.UserContext;
import work.work4.vo.UserVo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static work.work4.common.RedisConstants.LOGIN_TOKEN_KEY;

@Slf4j
@Service
public class UserService implements UserServiceInterface {
    @Resource
    private UserMapper userMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private FileUtils fileUtil;
    @Override
    public void register(String username, String password) {
//        加密注册
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setRole("USER");
        userMapper.insert(user);
    }
    @Override
    public RestBean<Object> login(String username, String password) {
        // 1. 创建未认证的令牌
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication authenticate= authenticationManager.authenticate(authToken);
        if(Objects.isNull(authenticate)){
            return RestBean.error("登录失败");
        }
//        认证成功
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        User user=loginUser.getUser();
        String token = JwtUtils.createJwt(loginUser);
        UserVo vo = new UserVo();
        BeanUtils.copyProperties(user, vo);
        vo.setToken(token);
        // 4. 存入 Redis 并设置过期时间 (例如 30 天)
        String redisKey = LOGIN_TOKEN_KEY + token;
        stringRedisTemplate.opsForValue().set(redisKey, vo.getId(), 30, TimeUnit.DAYS);
        return RestBean.success(vo);
    }

    @Override
    public RestBean<Object> getUser(String userId,String token) {
        // 拿到当前登录者的真实 ID
        String currentLoginId = UserContext.getUserId();
        // 2. 判断当前登录人是否有权操作这个 userId
        // 只能查自己的信息
        if (!currentLoginId.equals(userId)) {
            return RestBean.error("用户信息不匹配");
        }
        // 3.此时 userId 已经被验证是属于当前登录人的
        User user = userMapper.selectById(userId);
        if (user == null) {
            return RestBean.error("用户不存在");
        }
        UserVo vo = new UserVo();
        BeanUtils.copyProperties(user, vo);
        vo.setId(user.getId());
        return RestBean.success(vo);
    }

    @Override
    public RestBean<Object> uploadAvatar(MultipartFile file,String token) throws IOException{
        try (InputStream is = file.getInputStream()) {
            // 尝试读取图片，如果不是真正的图片格式，ImageIO 会返回 null
            BufferedImage bi = ImageIO.read(is);
            if (bi == null || bi.getWidth() <= 0 || bi.getHeight() <= 0) {
                return RestBean.error("上传文件内容损坏或不是合法的图片");
            }
        } catch (IOException e) {
            return RestBean.error("图片解析异常");
        }
        // 拿到当前登录者的真实 ID
        String currentLoginId = UserContext.getUserId();
        User user = userMapper.selectById(currentLoginId);
        user.setAvatarUrl(fileUtil.uploadPicture(file));
        userMapper.updateById(user);
        UserVo vo = new UserVo();
        BeanUtils.copyProperties(user, vo);
        return RestBean.success(vo);
    }
}
