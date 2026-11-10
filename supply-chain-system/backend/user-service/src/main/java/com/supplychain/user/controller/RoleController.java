package com.supplychain.user.controller;

import com.supplychain.common.result.Result;
import com.supplychain.user.service.RoleService;
import com.supplychain.user.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 查询所有角色列表
     */
    @GetMapping
    public Result<List<RoleVO>> list() {
        return Result.success(roleService.list());
    }
}
