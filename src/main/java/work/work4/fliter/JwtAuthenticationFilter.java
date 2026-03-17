package work.work4.fliter;

import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import work.work4.util.JwtUtils;
import work.work4.util.UserContext;

import java.io.IOException;

import static work.work4.common.RedisConstants.LOGIN_TOKEN_KEY;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //首先从Header中取出JWT
        String authorization = request.getHeader("Access-Token");
        //判断是否包含JWT且格式正确
        if (authorization != null) {
            String token = authorization;
            //开始解析成UserDetails对象，如果得到的是null说明解析失败，JWT有问题
            UserDetails user = JwtUtils.resolveJwt(token);
            String redisKey = LOGIN_TOKEN_KEY + token;
            String userId = stringRedisTemplate.opsForValue().get(redisKey);
            if(StringUtils.hasText(userId) && user!=null) {
                //验证没有问题，那么就可以开始创建Authentication了，这里我们跟默认情况保持一致
                //使用UsernamePasswordAuthenticationToken作为实体，填写相关用户信息进去
                UserContext.setUserId(userId);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                //然后直接把配置好的Authentication塞给SecurityContext表示已经完成验证
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        try {
            // 继续执行后续过滤器
            filterChain.doFilter(request, response);
        } finally {
            // 6. 关键：请求结束后清理 ThreadLocal
            UserContext.remove();
        }
    }
}
