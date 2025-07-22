package org.demo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.demo.pojo.Payload;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * 快速创建jwt令牌的工具类
 *
 * @author : Tomatos
 * @date : 2025/7/17
 */
public class JwtUtil {

    public static <T> String createJws(Long expiration, String secret, Payload<T> payload) {
        if (expiration == null || payload == null || secret == null)
                throw new NullPointerException();

        // jwsProperties之中的过期时间单位是秒, 这里需要转换为毫秒
        long jwsExpiration = System.currentTimeMillis() + expiration * 1000L;
        return Jwts.builder()
                   .expiration(new Date(jwsExpiration))
                   .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                   .claims(payload.getClaims())
                   .compact();
    }

    /**
     * 随机生成一个使用HS256加密的秘钥字符串
     *
     * @return java.lang.String
     * @since : 1.0
     * @author : Tomatos
     * @date : 2025/7/22 20:37
     */
    public static String getSecretKeyStr() {
        SecretKey secretKey = Jwts.SIG.HS256.key().build();
        byte[] encoded = secretKey.getEncoded();

        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(encoded);
    }

    /**
     * 解析传入的jwt是否与生成的时候一致
     *
     * @param secret 秘钥字
     * @param jws 加密之后的jwt
     * @return io.jsonwebtoken.Jws<io.jsonwebtoken.Claims>
     * @since : 1.0
     * @author : Tomatos
     * @date : 2025/7/22 20:38
     */
    public static Jws<Claims> parseVerifyJws(String secret, String jws) throws JwtException{
        return Jwts.parser()
                   .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                   .build() // 构建一个使用secretKey验证JWS的Parser对象
                   .parseSignedClaims(jws); // 解析得到 payload
    }
}
