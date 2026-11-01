package org.demo.jwt.dual.controller;

import org.demo.jwt.dual.context.UserContext;
import org.demo.jwt.dual.context.UserInfo;
import org.demo.jwt.dual.dto.ProtectedResourceResponse;
import org.demo.jwt.dual.dto.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 受保护资源，只有携带有效 access token 才能访问。
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
@RestController
public class ResourceController {

    @GetMapping("/api/protected")
    public Result<ProtectedResourceResponse> protectedResource() {
        UserInfo userInfo = UserContext.get();
        ProtectedResourceResponse data = new ProtectedResourceResponse(
                "Hello, " + userInfo.username() + "!",
                userInfo.userId(),
                userInfo.username()
        );
        return Result.success(data);
    }
}
