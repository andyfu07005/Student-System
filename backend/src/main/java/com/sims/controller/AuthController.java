package com.sims.controller;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sims.common.ApiResponse;
import com.sims.common.BusinessException;
import com.sims.config.JwtUtil;
import com.sims.entity.Role;
import com.sims.entity.User;
import com.sims.entity.UserRole;
import com.sims.mapper.RoleMapper;
import com.sims.mapper.UserMapper;
import com.sims.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final JwtUtil jwtUtil;

    @Value("${login.max-fail-count:5}")
    private int maxFailCount;
    @Value("${login.lock-duration-minutes:30}")
    private int lockMinutes;

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest req) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getUsername, req.username);
        User user = userMapper.selectOne(qw);

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查是否被锁定
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException("账号已被锁定，请" + lockMinutes + "分钟后重试");
        }

        // 检查是否被禁用
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 验证密码
        if (!BCrypt.checkpw(req.password, user.getPassword())) {
            int fails = (user.getLoginFail() == null ? 0 : user.getLoginFail()) + 1;
            user.setLoginFail(fails);
            if (fails >= maxFailCount) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockMinutes));
            }
            userMapper.updateById(user);
            throw new BusinessException("用户名或密码错误");
        }

        // 登录成功，清除失败计数
        user.setLoginFail(0);
        user.setLockedUntil(null);
        user.setLastLogin(LocalDateTime.now());
        userMapper.updateById(user);

        String roleCode = lookupRoleCode(user.getId());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roleCode);
        return ApiResponse.ok(Map.of(
                "token", token,
                "userId", user.getId(),
                "username", user.getUsername(),
                "roleCode", roleCode
        ));
    }

    private String lookupRoleCode(Long userId) {
        UserRole ur = userRoleMapper.selectOne(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (ur != null) {
            Role role = roleMapper.selectById(ur.getRoleId());
            if (role != null) return role.getRoleCode();
        }
        return "STUDENT";
    }

    public record LoginRequest(String username, String password) {}
}
