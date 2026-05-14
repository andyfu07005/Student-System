package com.sims.config;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sims.entity.Role;
import com.sims.entity.User;
import com.sims.entity.UserRole;
import com.sims.mapper.RoleMapper;
import com.sims.mapper.UserMapper;
import com.sims.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleMapper roleMapper;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public void run(String... args) {
        initRoles();
        initAdminUser();
    }

    private void initRoles() {
        if (roleMapper.selectCount(new LambdaQueryWrapper<>()) > 0) return;
        roleMapper.insert(role("ADMIN", "管理员", "系统管理员", 1));
        roleMapper.insert(role("TEACHER", "教师", "教师角色", 2));
        roleMapper.insert(role("STUDENT", "学生", "学生角色", 3));
        log.info("默认角色初始化完成");
    }

    private void initAdminUser() {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getUsername, "admin");
        if (userMapper.selectCount(qw) > 0) return;

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(BCrypt.hashpw("admin123", BCrypt.gensalt()));
        admin.setRealName("系统管理员");
        admin.setStatus(1);
        userMapper.insert(admin);

        Role adminRole = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, "ADMIN"));
        if (adminRole != null) {
            UserRole ur = new UserRole();
            ur.setUserId(admin.getId());
            ur.setRoleId(adminRole.getId());
            userRoleMapper.insert(ur);
        }
        log.info("默认管理员用户初始化完成 (admin / admin123)");
    }

    private Role role(String code, String name, String desc, int sort) {
        Role r = new Role();
        r.setRoleCode(code);
        r.setRoleName(name);
        r.setDescription(desc);
        r.setSort(sort);
        r.setStatus(1);
        return r;
    }
}
