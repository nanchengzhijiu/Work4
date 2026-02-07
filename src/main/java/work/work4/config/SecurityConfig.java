package work.work4.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import work.work4.common.RestBean;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.AccessDeniedException;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(conf -> {
                    conf.anyRequest().authenticated();
                })
                .formLogin(conf -> {
                    //一般分离之后，为了统一规范接口，使用 /api/模块/功能 的形式命名接口
                    conf.loginProcessingUrl("/index");
                    conf.failureHandler(this::handleProcess);
                    conf.successHandler(this::handleProcess);
                    conf.permitAll();
                })
                .exceptionHandling(conf -> {
                    //配置授权相关异常处理器
                    conf.accessDeniedHandler(this::handleProcess);
                    //配置验证相关异常的处理器
                    conf.authenticationEntryPoint(this::handleProcess);
                })
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }
    private void handleProcess(HttpServletRequest request,
                               HttpServletResponse response,
                               Object exceptionOrAuthentication) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        if(exceptionOrAuthentication instanceof AccessDeniedException exception) {
            writer.write(RestBean.failure(403, exception.getMessage()).asJsonString());
        } else if(exceptionOrAuthentication instanceof Exception exception) {
            writer.write(RestBean.failure(401, exception.getMessage()).asJsonString());
        } else if(exceptionOrAuthentication instanceof Authentication authentication){
            writer.write(RestBean.success(authentication.getName()).asJsonString());
        }
    }
}
