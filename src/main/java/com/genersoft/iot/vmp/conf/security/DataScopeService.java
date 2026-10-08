package com.genersoft.iot.vmp.conf.security;

import com.genersoft.iot.vmp.conf.security.dto.LoginUser;
import com.genersoft.iot.vmp.gb28181.bean.Region;
import com.genersoft.iot.vmp.gb28181.dao.CommonGBChannelMapper;
import com.genersoft.iot.vmp.gb28181.dao.RegionMapper;
import com.genersoft.iot.vmp.service.IUserRegionService;
import com.genersoft.iot.vmp.storager.dao.dto.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户-区域数据范围服务
 * <p>
 * 在 Controller/Service 上使用：@Autowired private DataScopeService dataScope;
 * <p>
 * 规则：
 * 1. role_id = 1（超级管理员）不受限制；
 * 2. 非超管但未绑定任何区域时视为不限制（保持向后兼容）；
 * 3. 非超管且绑定了区域时，只能访问"绑定区域及其所有子孙区域"对应的通道。
 */
@Component("dataScope")
public class DataScopeService {

    @Autowired
    private IUserRegionService userRegionService;

    @Autowired
    private RegionMapper regionMapper;

    @Autowired
    private CommonGBChannelMapper commonGBChannelMapper;

    /**
     * 当前登录用户是否为超级管理员
     */
    public boolean isSuperAdmin() {
        LoginUser loginUser = SecurityUtils.getUserInfo();
        if (loginUser == null) {
            return false;
        }
        Role role = loginUser.getRole();
        return role != null && role.getId() == PermissionService.SUPER_ADMIN_ROLE_ID;
    }

    /**
     * 当前用户是否受限：非超级管理员，且绑定了至少一个区域
     */
    public boolean isRestricted() {
        LoginUser loginUser = SecurityUtils.getUserInfo();
        if (loginUser == null) {
            // 非登录上下文（如内部任务）不做限制，保持向后兼容
            return false;
        }
        Role role = loginUser.getRole();
        if (role != null && role.getId() == PermissionService.SUPER_ADMIN_ROLE_ID) {
            return false;
        }
        List<Integer> regionIds = userRegionService.getRegionIdsByUserId(loginUser.getId());
        return regionIds != null && !regionIds.isEmpty();
    }

    /**
     * 当前用户允许访问的行政区划编码集合
     * <p>
     * 不受限时返回 null，表示不做过滤（行为与改造前一致）。
     */
    public Set<String> getAllowedCivilCodes() {
        if (!isRestricted()) {
            return null;
        }
        LoginUser loginUser = SecurityUtils.getUserInfo();
        List<Integer> regionIds = userRegionService.getRegionIdsByUserId(loginUser.getId());
        if (regionIds == null || regionIds.isEmpty()) {
            return null;
        }
        // 一次性查出全部区域节点，在内存中按 parent_id 树递归展开子孙节点
        List<Region> allRegionList = regionMapper.query(null, null);
        Map<Integer, Region> regionMap = new HashMap<>(allRegionList.size());
        Map<Integer, List<Region>> childrenMap = new HashMap<>();
        for (Region region : allRegionList) {
            regionMap.put(region.getId(), region);
            if (region.getParentId() != null) {
                childrenMap.computeIfAbsent(region.getParentId(), k -> new ArrayList<>()).add(region);
            }
        }
        Set<String> allowedCivilCodes = new HashSet<>();
        Set<Integer> visited = new HashSet<>();
        Deque<Integer> queue = new ArrayDeque<>(regionIds);
        while (!queue.isEmpty()) {
            Integer regionId = queue.poll();
            if (regionId == null || !visited.add(regionId)) {
                continue;
            }
            Region region = regionMap.get(regionId);
            if (region == null) {
                continue;
            }
            if (region.getDeviceId() != null) {
                allowedCivilCodes.add(region.getDeviceId());
            }
            List<Region> children = childrenMap.get(regionId);
            if (children != null) {
                for (Region child : children) {
                    queue.add(child.getId());
                }
            }
        }
        return allowedCivilCodes;
    }

    /**
     * 当前用户是否允许访问指定通道（按 wvp_device_channel.id）
     */
    public boolean isChannelAllowed(int channelId) {
        if (!isRestricted()) {
            return true;
        }
        Set<String> allowedCivilCodes = getAllowedCivilCodes();
        if (allowedCivilCodes == null) {
            return true;
        }
        String civilCode = commonGBChannelMapper.queryCivilCodeById(channelId);
        return civilCode != null && allowedCivilCodes.contains(civilCode);
    }
}
