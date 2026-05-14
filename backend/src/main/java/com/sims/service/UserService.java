package com.sims.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sims.common.PageResult;
import com.sims.common.exception.BusinessException;
import com.sims.entity.Role;
import com.sims.entity.User;
import com.sims.entity.UserRole;
import com.sims.mapper.RoleMapper;
import com.sims.mapper.UserMapper;
import com.sims.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    public PageResult<User> listPage(int page, int size, String keyword, String roleCode) {
        IPage<User> ipage = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 模糊搜索：用户名 / 真实姓名
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                              .or().like(User::getRealName, keyword));
        }
        // 按角色筛选 — 对 roleCode 做白名单校验防止注入
        if (roleCode != null && !roleCode.isBlank()) {
            String safeCode = roleCode.toUpperCase();
            if (!safeCode.matches("^[A-Z_]{2,32}$")) {
                throw new BusinessException("无效的角色编码");
            }
            wrapper.exists("SELECT 1 FROM sys_user_role ur " +
                    "JOIN sys_role r ON ur.role_id = r.id " +
                    "WHERE ur.user_id = sys_user.id AND r.role_code = '" +
                    safeCode + "'");
        }

        wrapper.orderByDesc(User::getCreatedAt);
        IPage<User> result = userMapper.selectPageWithRole(ipage, wrapper);
        return PageResult.of(result);
    }

    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 查询角色
        LambdaQueryWrapper<UserRole> urw = new LambdaQueryWrapper<>();
        urw.eq(UserRole::getUserId, id);
        UserRole userRole = userRoleMapper.selectOne(urw);
        if (userRole != null) {
            Role role = roleMapper.selectById(userRole.getRoleId());
            if (role != null) {
                user.setRoleCode(role.getRoleCode());
            }
        }
        user.setPassword(null);
        return user;
    }

    @Transactional
    public User create(User user) {
        // 用户名唯一性校验
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getUsername, user.getUsername());
        if (userMapper.selectCount(qw) > 0) {
            throw new BusinessException("用户名已存在");
        }
        // 角色校验
        String roleCode = user.getRoleCode();
        if (roleCode == null || roleCode.isBlank()) {
            throw new BusinessException("角色不能为空");
        }
        Role role = getRoleByCode(roleCode);
        if (role == null) {
            throw new BusinessException("角色[" + roleCode + "]不存在");
        }
        // 密码加密
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword("123456"); // 默认密码
        }
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        user.setStatus(1);
        userMapper.insert(user);

        // 绑定角色
        UserRole ur = new UserRole();
        ur.setUserId(user.getId());
        ur.setRoleId(role.getId());
        userRoleMapper.insert(ur);

        user.setPassword(null);
        user.setRoleCode(roleCode);
        return user;
    }

    @Transactional
    public User update(Long id, User req) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 用户名唯一性校验
        if (req.getUsername() != null && !req.getUsername().isBlank()
                && !req.getUsername().equals(user.getUsername())) {
            LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
            qw.eq(User::getUsername, req.getUsername());
            if (userMapper.selectCount(qw) > 0) {
                throw new BusinessException("用户名已存在");
            }
            user.setUsername(req.getUsername());
        }
        if (req.getRealName() != null) user.setRealName(req.getRealName());
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getPhone() != null) user.setPhone(req.getPhone());

        userMapper.updateById(user);

        // 角色变更
        if (req.getRoleCode() != null && !req.getRoleCode().isBlank()) {
            Role role = getRoleByCode(req.getRoleCode());
            if (role == null) {
                throw new BusinessException("角色[" + req.getRoleCode() + "]不存在");
            }
            LambdaQueryWrapper<UserRole> urw = new LambdaQueryWrapper<>();
            urw.eq(UserRole::getUserId, id);
            userRoleMapper.delete(urw);

            UserRole ur = new UserRole();
            ur.setUserId(id);
            ur.setRoleId(role.getId());
            userRoleMapper.insert(ur);

            user.setRoleCode(req.getRoleCode());
        }
        user.setPassword(null);
        return user;
    }

    @Transactional
    public void delete(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 逻辑删除用户
        userMapper.deleteById(id);
        // 清理角色关联
        LambdaQueryWrapper<UserRole> urw = new LambdaQueryWrapper<>();
        urw.eq(UserRole::getUserId, id);
        userRoleMapper.delete(urw);
    }

    public void updateStatus(Long id, Integer status) {
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值无效，仅支持 0(禁用) / 1(启用)");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        // 解禁时清除锁定状态
        if (status == 1) {
            user.setLoginFail(0);
            user.setLockedUntil(null);
        }
        userMapper.updateById(user);
    }

    public void resetPassword(Long id, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("密码长度不能小于6位");
        }
        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        user.setLoginFail(0);
        user.setLockedUntil(null);
        userMapper.updateById(user);
    }

    private Role getRoleByCode(String roleCode) {
        LambdaQueryWrapper<Role> qw = new LambdaQueryWrapper<>();
        qw.eq(Role::getRoleCode, roleCode.toUpperCase());
        return roleMapper.selectOne(qw);
    }
}
