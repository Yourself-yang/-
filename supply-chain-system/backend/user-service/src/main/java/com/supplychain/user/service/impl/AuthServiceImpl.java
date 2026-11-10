package com.supplychain.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplychain.common.exception.BusinessException;
import com.supplychain.common.jwt.JwtUtil;
import com.supplychain.common.util.PasswordUtil;
import com.supplychain.user.dto.LoginDTO;
import com.supplychain.user.dto.PasswordDTO;
import com.supplychain.user.entity.Role;
import com.supplychain.user.entity.User;
import com.supplychain.user.mapper.UserMapper;
import com.supplychain.user.service.AuthService;
import com.supplychain.user.service.RoleService;
import com.supplychain.user.vo.LoginVO;
import com.supplychain.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RoleService roleService;
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private static final String TOKEN_BLACKLIST_KEY = "token:blacklist:";

    @Override
    public LoginVO login(LoginDTO dto) {
        String username = dto.getUsername();

        // 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null || user.getDeleted() != 0 || user.getStatus() != 1) {
            throw new BusinessException("用户名或密码错误");
        }

        // 校验密码
        if (!PasswordUtil.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 获取角色和权限
        Role role = roleService.getRoleByUserId(user.getId());
        String roleCode = role != null ? role.getRoleCode() : "";
        List<String> permissions = StrUtil.isNotBlank(roleCode)
                ? roleService.getPermissionsByRoleCode(roleCode)
                : List.of();

        // 生成token
        String token = JwtUtil.generateToken(jwtSecret, jwtExpiration, user.getId(), user.getUsername(), roleCode,
                permissions);

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(roleCode);
        vo.setPermissions(permissions);
        return vo;
    }

    @Override
    public void logout(String token) {
        if (StrUtil.isBlank(token)) {
            return;
        }
        // 加入黑名单，24小时过期
        String blacklistKey = TOKEN_BLACKLIST_KEY + token;
        redisTemplate.opsForValue().set(blacklistKey, "1", 24, TimeUnit.HOURS);
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() != 0) {
            throw new BusinessException("用户不存在");
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());

        Role role = roleService.getRoleByUserId(userId);
        if (role != null) {
            vo.setRoleCode(role.getRoleCode());
            vo.setRoleName(role.getRoleName());
        }
        return vo;
    }

    @Override
    public void changePassword(Long userId, PasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 校验旧密码
        if (!PasswordUtil.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        // 更新密码
        user.setPassword(PasswordUtil.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

}
