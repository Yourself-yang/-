package com.supplychain.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.supplychain.common.result.PageResult;
import com.supplychain.common.result.Result;
import com.supplychain.user.dto.UserCreateDTO;
import com.supplychain.user.dto.UserUpdateDTO;
import com.supplychain.user.entity.User;
import com.supplychain.user.service.AuthService;
import com.supplychain.user.service.UserService;
import com.supplychain.user.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/page")
    public Result<PageResult<UserVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status) {
        IPage<UserVO> p = userService.page(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize), keyword, roleCode,
                status);
        return Result.success(PageResult.of(p.getTotal(), p.getRecords()));
    }

    @GetMapping
    public Result<PageResult<UserVO>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status) {
        return page(pageNum, pageSize, keyword, roleCode, status);
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody UserCreateDTO dto) {
        userService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id:\\d+}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UserUpdateDTO dto) {
        userService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id:\\d+}/role")
    public Result<Void> assignRoleBody(@PathVariable Long id, @RequestBody RoleAssignBody body) {
        userService.assignRole(id, body.getRoleCode());
        return Result.success();
    }

    @PutMapping("/{id:\\d+}/roles")
    public Result<Void> assignRole(@PathVariable Long id, @RequestParam(required = false) String roleCode,
            @RequestBody(required = false) RoleAssignBody body) {
        String rc = roleCode;
        if (rc == null && body != null)
            rc = body.getRoleCode();
        userService.assignRole(id, rc);
        return Result.success();
    }

    @GetMapping("/{id:\\d+}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @GetMapping("/me")
    public Result<UserVO> me(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        try {
            return Result.success(authService.getCurrentUser(userId));
        } catch (Exception e) {
            return Result.error(401, "用户不存在");
        }
    }

    public static class RoleAssignBody {
        private String roleCode;

        public String getRoleCode() {
            return roleCode;
        }

        public void setRoleCode(String roleCode) {
            this.roleCode = roleCode;
        }
    }
}
