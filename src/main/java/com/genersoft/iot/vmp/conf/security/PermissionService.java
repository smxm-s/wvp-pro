package com.genersoft.iot.vmp.conf.security;

import com.genersoft.iot.vmp.conf.UserSetting;
import com.genersoft.iot.vmp.conf.security.dto.LoginUser;
import com.genersoft.iot.vmp.storager.dao.dto.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限校验服务
 * <p>
 * 在 Controller 方法上使用：@PreAuthorize("@perm.has('device:edit')")
 * <p>
 * 规则：
 * 1. 未开启接口鉴权（interface-authentication=false）时不做权限校验；
 * 2. role_id = 1（超级管理员）拥有全部权限；
 * 3. 其他角色从 wvp_user_role.authority 读取逗号分隔的权限码。
 */
@Component("perm")
public class PermissionService {

    /**
     * 超级管理员角色ID，拥有全部权限
     */
    public static final int SUPER_ADMIN_ROLE_ID = 1;

    @Autowired
    private UserSetting userSetting;

    /**
     * 是否拥有指定权限
     */
    public boolean has(String code) {
        if (!Boolean.TRUE.equals(userSetting.getInterfaceAuthentication())) {
            // 未开启接口鉴权，不做权限校验
            return true;
        }
        if (code == null || code.isEmpty()) {
            return true;
        }
        return getCurrentPermissions().contains(code);
    }

    /**
     * 是否拥有其中任意一个权限
     */
    public boolean hasAny(String... codes) {
        if (codes == null || codes.length == 0) {
            return true;
        }
        Set<String> owned = getCurrentPermissions();
        return Arrays.stream(codes).anyMatch(owned::contains);
    }

    /**
     * 当前登录用户的权限码集合
     */
    public Set<String> getCurrentPermissions() {
        LoginUser loginUser = SecurityUtils.getUserInfo();
        if (loginUser == null) {
            return Collections.emptySet();
        }
        return parse(loginUser.getRole());
    }

    /**
     * 解析角色拥有的权限码
     */
    public static Set<String> parse(Role role) {
        if (role == null) {
            return Collections.emptySet();
        }
        if (role.getId() == SUPER_ADMIN_ROLE_ID) {
            // 超级管理员拥有全部权限
            return Arrays.stream(Permission.values())
                    .map(Permission::getCode)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        if (role.getAuthority() == null || role.getAuthority().isEmpty()) {
            return Collections.emptySet();
        }
        return Arrays.stream(role.getAuthority().split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
