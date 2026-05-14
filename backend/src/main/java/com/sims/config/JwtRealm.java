package com.sims.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sims.entity.Role;
import com.sims.entity.User;
import com.sims.entity.UserRole;
import com.sims.mapper.RoleMapper;
import com.sims.mapper.UserMapper;
import com.sims.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRealm extends AuthorizingRealm {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final JwtUtil jwtUtil;

    @Override
    public boolean supports(org.apache.shiro.authc.AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /** 认证：验证 JWT 并检查用户状态 */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
            throws AuthenticationException {
        String jwt = (String) token.getCredentials();
        if (!jwtUtil.validate(jwt)) {
            throw new ExpiredCredentialsException("Token已过期或无效");
        }
        Long userId = jwtUtil.getUserId(jwt);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UnknownAccountException("用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new DisabledAccountException("账号已被禁用，请联系管理员");
        }

        return new SimpleAuthenticationInfo(user, jwt, getName());
    }

    /** 授权：查询用户角色 */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        User user = (User) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        LambdaQueryWrapper<UserRole> urw = new LambdaQueryWrapper<>();
        urw.eq(UserRole::getUserId, user.getId());
        UserRole userRole = userRoleMapper.selectOne(urw);
        if (userRole != null) {
            Role role = roleMapper.selectById(userRole.getRoleId());
            if (role != null) {
                info.addRole(role.getRoleCode());
            }
        }
        return info;
    }
}
