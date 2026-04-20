package work.work4.config;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import work.work4.common.LoginUser;
import work.work4.common.RestBean;
import work.work4.fliter.JwtAuthenticationFilter;
import work.work4.mapper.UserMapper;
import work.work4.pojo.User;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
    @Resource
    private UserMapper userMapper;
    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // 1. 从数据库查询用户
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, username));

            if (user == null) {
                throw new UsernameNotFoundException("用户不存在");
            }

            // 2. 获取用户角色并处理前缀
            // 假设 user.getRole() 返回 "ADMIN" 或 "USER"
            String role = user.getRole();

            // Spring Security 默认要求角色权限必须以 "ROLE_" 开头
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + role)
            );

            // 3. 返回封装后的 LoginUser
            // 注意：你的 LoginUser 构造函数需要接收这个 authorities 列表
            return new LoginUser(user, authorities);
        };
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // 必须禁用 CSRF，否则 POST 请求会被拦截
                .sessionManagement(conf -> conf.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 无状态
                .authorizeHttpRequests(conf -> {
                    // 允许登录、注册、静态资源直接访问
                    conf.requestMatchers("/admin/**").hasRole("ADMIN")
                            .requestMatchers("/user/login","/user/register","/video/feed", "/css/**", "/js/**","/ws")
                            .permitAll();
                    conf.anyRequest().authenticated();

                }) 
                .exceptionHandling(conf -> {
                    conf.accessDeniedHandler(this::handleError);
                    conf.authenticationEntryPoint(this::handleError);
                })
                // 将 JWT 过滤器放在用户名密码过滤器之前
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    // 统一处理异常返回
    private void handleError(HttpServletRequest request,
                             HttpServletResponse response,
                             Object exception) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        RestBean<Void> errorRes;
        if (exception instanceof AccessDeniedException) {
            errorRes=RestBean.error("forbidden");
        } else {
            errorRes=RestBean.error("unauthorized");
        }
        writer.write(JSON.toJSONString(errorRes));
    }
}
