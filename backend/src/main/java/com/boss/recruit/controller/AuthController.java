package com.boss.recruit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boss.recruit.common.Result;
import com.boss.recruit.config.JwtUtil;
import com.boss.recruit.entity.User;
import com.boss.recruit.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录注册接口
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("username", username);
        User u = userMapper.selectOne(qw);
        if (u == null || !u.getPassword().equals(password)) {
            return Result.error("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(u.getId(), u.getUsername(), u.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        u.setPassword(null);
        data.put("user", u);
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("username", user.getUsername());
        if (userMapper.selectCount(qw) > 0) {
            return Result.error("用户名已存在");
        }
        if (user.getRole() == null) user.setRole("SEEKER");
        userMapper.insert(user);
        user.setPassword(null);
        return Result.success(user);
    }

    @GetMapping("/info")
    public Result<User> info(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) return Result.error("未登录");
        try {
            Long uid = jwtUtil.parse(token.substring(7)).get("userId", Long.class);
            User u = userMapper.selectById(uid);
            u.setPassword(null);
            return Result.success(u);
        } catch (Exception e) {
            return Result.error("token无效");
        }
    }
}
