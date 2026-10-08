package com.genersoft.iot.vmp.vmanager.user;

import com.genersoft.iot.vmp.conf.exception.ControllerException;
import com.genersoft.iot.vmp.conf.security.JwtUtils;
import com.genersoft.iot.vmp.conf.security.Permission;
import com.genersoft.iot.vmp.conf.security.PermissionService;
import com.genersoft.iot.vmp.conf.security.SecurityUtils;
import com.genersoft.iot.vmp.service.IRoleService;
import com.genersoft.iot.vmp.storager.dao.dto.Role;
import com.genersoft.iot.vmp.utils.DateUtil;
import com.genersoft.iot.vmp.vmanager.bean.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name  = "角色管理")

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private IRoleService roleService;

    @PostMapping("/add")
    @PreAuthorize("@perm.has('user:edit')")
    @Operation(summary = "添加角色", security = @SecurityRequirement(name = JwtUtils.HEADER))
    @Parameter(name = "name", description = "角色名", required = true)
    @Parameter(name = "authority", description = "权限码，逗号分隔", required = true)
    public void add(@RequestParam String name,
                                                  @RequestParam(required = false) String authority){
        checkAdmin();

        Role role = new Role();
        role.setName(name);
        role.setAuthority(authority);
        role.setCreateTime(DateUtil.getNow());
        role.setUpdateTime(DateUtil.getNow());

        int addResult = roleService.add(role);
        if (addResult <= 0) {
            throw new ControllerException(ErrorCode.ERROR100);
        }
    }

    @PostMapping("/update")
    @PreAuthorize("@perm.has('user:edit')")
    @Operation(summary = "修改角色", security = @SecurityRequirement(name = JwtUtils.HEADER))
    @Parameter(name = "id", description = "角色Id", required = true)
    @Parameter(name = "name", description = "角色名", required = true)
    @Parameter(name = "authority", description = "权限码，逗号分隔")
    public void update(@RequestParam Integer id,
                       @RequestParam String name,
                       @RequestParam(required = false) String authority){
        checkAdmin();
        if (id == PermissionService.SUPER_ADMIN_ROLE_ID) {
            // 超级管理员角色不允许修改
            throw new ControllerException(ErrorCode.ERROR100.getCode(), "超级管理员角色不允许修改");
        }
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        role.setAuthority(authority);
        role.setUpdateTime(DateUtil.getNow());
        int updateResult = roleService.update(role);
        if (updateResult <= 0) {
            throw new ControllerException(ErrorCode.ERROR100);
        }
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@perm.has('user:edit')")
    @Operation(summary = "删除角色", security = @SecurityRequirement(name = JwtUtils.HEADER))
    @Parameter(name = "id", description = "角色Id", required = true)
    public void delete(@RequestParam Integer id){
        checkAdmin();
        int deleteResult = roleService.delete(id);

        if (deleteResult <= 0) {
            throw new ControllerException(ErrorCode.ERROR100);
        }
    }

    @GetMapping("/all")
    @PreAuthorize("@perm.has('user:view')")
    @Operation(summary = "查询角色", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public List<Role> all(){
        return roleService.getAll();
    }

    @GetMapping("/permission/all")
    @PreAuthorize("@perm.has('user:view')")
    @Operation(summary = "查询全部权限点（按分组）", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public List<Map<String, Object>> allPermission(){
        return Permission.allGrouped();
    }

    /**
     * 仅超级管理员可增删改角色
     */
    private void checkAdmin() {
        if (SecurityUtils.getUserInfo().getRole().getId() != PermissionService.SUPER_ADMIN_ROLE_ID) {
            throw new ControllerException(ErrorCode.ERROR403);
        }
    }
}
