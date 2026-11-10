package com.supplychain.user.controller;

import com.supplychain.common.result.Result;
import com.supplychain.user.dto.LoginDTO;
import com.supplychain.user.dto.PasswordDTO;
import com.supplychain.user.service.AuthService;
import com.supplychain.user.vo.LoginVO;
import com.supplychain.user.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader);
        authService.logout(token);
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> info(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(authService.getCurrentUser(userId));
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                       @Valid @RequestBody PasswordDTO dto) {
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        authService.changePassword(userId, dto);
        return Result.success();
    }

    /**
     * 从Authorization头提取token（去掉Bearer前缀）
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }
}
