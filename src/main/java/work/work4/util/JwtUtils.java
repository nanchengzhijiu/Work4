package work.work4.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import work.work4.common.LoginUser;

import java.util.*;
import java.util.stream.Collectors;

public class JwtUtils {
    //Jwt秘钥
    private static final String key = "abcdefghijklmn";
    //根据用户信息创建Jwt令牌
    public static String createJwt(LoginUser user){
        Algorithm algorithm = Algorithm.HMAC256(key);
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.SECOND, 3600 * 24 * 7);
        System.out.println(user.getAuthorities());
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("name", user.getUsername())  //配置JWT自定义信息
                .withClaim("id",user.getUser().getId())
                .withClaim("authorities", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(calendar.getTime())  //设置过期时间
                .withIssuedAt(now)    //设置创建创建时间
                .sign(algorithm);   //最终签名
    }
    //根据Jwt验证并解析用户信息
    public static UserDetails resolveJwt(String token) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT verify = jwtVerifier.verify(token);
            // 1. 获取自定义 Claims
            String id = verify.getClaim("id").asString();
            String username = verify.getClaim("name").asString();
            // 获取存进去的角色列表
            List<String> authStrings = verify.getClaim("authorities").asList(String.class);

            // 2. 还原权限对象
            List<SimpleGrantedAuthority> authorities = authStrings.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            // 3. 构建 POJO
            work.work4.pojo.User userPojo = new work.work4.pojo.User();
            userPojo.setId(id);
            userPojo.setUsername(username);
            // 4. 返回带权限的 LoginUser
            // 确保你的 LoginUser 构造函数支持传入 authorities
            return new LoginUser(userPojo, authorities);

        } catch (JWTVerificationException e) {
            return null;
        }
    }
}
