package com.sims.controller;

import com.sims.common.ApiResponse;
import com.sims.common.PageResult;
import com.sims.entity.User;
import com.sims.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 分页查询用户列表 — 管理员权限 */
    @GetMapping

public ApiResponse<PageResult<User>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleCode) {
        return ApiResponse.ok(userService.listPage(page, size, keyword, roleCode));
    }

    /** 查询单个用户 */
    @GetMapping("/{id}")

public ApiResponse<User> get(@PathVariable Long id) {
        return ApiResponse.ok(userService.getById(id));
    }

    /** 创建用户 */
    @PostMapping

public ApiResponse<User> create(@Valid @RequestBody CreateRequest req) {
        User user = new User();
        user.setUsername(req.username);
        user.setPassword(req.password);
        user.setRealName(req.realName);
        user.setEmail(req.email);
        user.setPhone(req.phone);
        user.setRoleCode(req.roleCode.toUpperCase());
        return ApiResponse.ok(userService.create(user));
    }

    /** 修改用户信息 */
    @PutMapping("/{id}")

public ApiResponse<User> update(@PathVariable Long id, @RequestBody UpdateRequest req) {
        User user = new User();
        user.setUsername(req.username);
        user.setRealName(req.realName);
        user.setEmail(req.email);
        user.setPhone(req.phone);
        if (req.roleCode != null) {
            user.setRoleCode(req.roleCode.toUpperCase());
        }
        return ApiResponse.ok(userService.update(id, user));
    }

    /** 删除用户 */
    @DeleteMapping("/{id}")

public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.ok();
    }

    /** 启用/禁用用户 */
    @PutMapping("/{id}/status")

public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null) {
            return ApiResponse.fail(400, "状态不能为空");
        }
        userService.updateStatus(id, status);
        return ApiResponse.ok();
    }

    /** 重置密码 */
    @PutMapping("/{id}/reset-password")

public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.isBlank()) {
            return ApiResponse.fail(400, "新密码不能为空");
        }
        userService.resetPassword(id, newPassword);
        return ApiResponse.ok();
    }

    // ---------- 请求 DTO ----------

    public record CreateRequest(
            @NotBlank(message = "用户名不能为空")
            @Size(min = 3, max = 64, message = "用户名长度3-64位")
            String username,

            @Size(min = 6, max = 64, message = "密码长度6-64位")
            String password,

            String realName,
            String email,
            String phone,

            @NotBlank(message = "角色不能为空")
            String roleCode
    ) {}

    public record UpdateRequest(
            String username,
            String realName,
            String email,
            String phone,
            String roleCode
    ) {}
}
