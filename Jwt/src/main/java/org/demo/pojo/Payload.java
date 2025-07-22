package org.demo.pojo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : Tomatos
 * @date : 2025/7/22
 */
public class Payload<T> {
    private final Map<String, T> claims = new HashMap<>();

    public Map<String, T> getClaims() {
        return claims;
    }

    public void addClaim(String key, T value) {
        claims.put(key, value);
    }
}
