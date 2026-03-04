package work.work4.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import work.work4.common.LoginUser;

import java.util.*;

public class JwtUtils {
    //Jwt秘钥
    private static final String key = "abcdefghijklmn";
    //根据用户信息创建Jwt令牌
    public static String createJwt(LoginUser user){
        Algorithm algorithm = Algorithm.HMAC256(key);
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.SECOND, 3600 * 24 * 7);
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
    public static UserDetails resolveJwt(String token){
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT verify = jwtVerifier.verify(token);
            Map<String, Claim> claims = verify.getClaims();
            if(new Date().after(claims.get("exp").asDate()))
                return null;
            work.work4.pojo.User userPojo = new work.work4.pojo.User();
            userPojo.setId(claims.get("id").asString());
            userPojo.setUsername(claims.get("name").asString());
            userPojo.setPassword("");
            return new LoginUser(userPojo);
        } catch (JWTVerificationException e) {
            return null;
        }
    }
}
