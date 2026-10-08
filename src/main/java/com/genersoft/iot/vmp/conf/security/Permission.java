package com.genersoft.iot.vmp.conf.security;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限点定义（命名规范：模块:操作）
 * <p>
 * 用于 @PreAuthorize("@perm.has('device:edit')") 鉴权，以及前端权限树渲染。
 * role_id = 1（超级管理员）拥有全部权限，无需在此列出。
 */
public enum Permission {

    DASHBOARD_VIEW("dashboard:view", "首页", "首页"),

    DEVICE_VIEW("device:view", "查看设备", "国标设备"),
    DEVICE_EDIT("device:edit", "编辑设备", "国标设备"),

    CHANNEL_PLAY("channel:play", "实时预览", "实时视频"),
    CHANNEL_PTZ("channel:ptz", "云台控制", "实时视频"),
    CHANNEL_RECORD("channel:record", "设备录像查询", "实时视频"),
    CHANNEL_BROADCAST("channel:broadcast", "语音对讲", "实时视频"),

    CLOUD_RECORD_VIEW("cloudRecord:view", "查看云端录像", "录像管理"),
    CLOUD_RECORD_DELETE("cloudRecord:delete", "删除云端录像", "录像管理"),
    RECORD_PLAN_VIEW("recordPlan:view", "查看录制计划", "录像管理"),
    RECORD_PLAN_EDIT("recordPlan:edit", "编辑录制计划", "录像管理"),

    PROXY_VIEW("proxy:view", "查看拉流代理", "流管理"),
    PROXY_EDIT("proxy:edit", "编辑拉流代理", "流管理"),
    PUSH_VIEW("push:view", "查看推流", "流管理"),
    PUSH_EDIT("push:edit", "编辑推流", "流管理"),

    PLATFORM_VIEW("platform:view", "查看国标级联", "国标级联"),
    PLATFORM_EDIT("platform:edit", "编辑国标级联", "国标级联"),

    ORG_VIEW("org:view", "查看通道管理", "通道管理"),
    ORG_EDIT("org:edit", "编辑通道管理", "通道管理"),

    MAP_VIEW("map:view", "电子地图", "电子地图"),

    ALARM_VIEW("alarm:view", "查看报警", "报警管理"),
    ALARM_HANDLE("alarm:handle", "处理报警", "报警管理"),

    JT_VIEW("jt:view", "查看部标设备", "部标设备"),
    JT_EDIT("jt:edit", "编辑部标设备", "部标设备"),

    MEDIA_SERVER_VIEW("mediaServer:view", "查看媒体节点", "系统管理"),
    MEDIA_SERVER_EDIT("mediaServer:edit", "编辑媒体节点", "系统管理"),
    USER_VIEW("user:view", "查看用户", "系统管理"),
    USER_EDIT("user:edit", "编辑用户", "系统管理"),
    API_KEY_VIEW("apiKey:view", "查看接口鉴权", "系统管理"),
    API_KEY_EDIT("apiKey:edit", "编辑接口鉴权", "系统管理"),
    LOG_VIEW("log:view", "查看日志", "系统管理"),
    SYSTEM_VIEW("system:view", "查看系统信息", "系统管理"),
    ;

    /**
     * 权限码，如 device:edit
     */
    private final String code;
    /**
     * 权限名称（中文）
     */
    private final String name;
    /**
     * 所属分组（用于前端权限树展示）
     */
    private final String group;

    Permission(String code, String name, String group) {
        this.code = code;
        this.name = name;
        this.group = group;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    /**
     * 按分组聚合全部权限点，供前端渲染权限树
     */
    public static List<Map<String, Object>> allGrouped() {
        Map<String, List<Map<String, String>>> grouped = new LinkedHashMap<>();
        for (Permission permission : values()) {
            grouped.computeIfAbsent(permission.group, k -> new ArrayList<>())
                    .add(Map.of("code", permission.code, "name", permission.name));
        }
        List<Map<String, Object>> result = new ArrayList<>();
        grouped.forEach((group, items) -> result.add(Map.of("group", group, "permissions", items)));
        return result;
    }
}
