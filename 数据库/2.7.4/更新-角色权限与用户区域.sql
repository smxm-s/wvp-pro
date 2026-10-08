-- ============================================================
-- 角色权限 + 用户数据范围（区域隔离）增量脚本
-- 适用：已按 2.7.4 建库的存量数据库
-- 内容：
--   1. 角色权限字段扩容（原 varchar(50) 存不下权限列表）
--   2. 写入「安全负责人 / 安全监督人」的权限码
--   3. 新建用户-区域绑定表（数据级权限）
-- 说明：role_id=1（admin）为超级管理员，代码中直接放行，无需配置权限码
-- ============================================================

-- 1. 角色权限字段扩容
ALTER TABLE wvp_user_role MODIFY COLUMN authority varchar(1000) NULL COMMENT '权限标识（逗号分隔的权限码）';

-- 2. 安全负责人（id=2）：可管理业务，但不能管平台/用户/媒体节点
UPDATE wvp_user_role SET authority = 'dashboard:view,device:view,device:edit,channel:play,channel:ptz,channel:record,channel:broadcast,cloudRecord:view,cloudRecord:delete,recordPlan:view,recordPlan:edit,proxy:view,proxy:edit,push:view,push:edit,platform:view,org:view,org:edit,map:view,alarm:view,alarm:handle,jt:view,jt:edit,mediaServer:view,log:view,system:view'
WHERE id = 2;

-- 3. 安全监督人（id=3）：只读 + 实时预览（含控制台所需的系统/节点状态查看权限）
UPDATE wvp_user_role SET authority = 'dashboard:view,device:view,channel:play,channel:record,cloudRecord:view,recordPlan:view,proxy:view,push:view,platform:view,org:view,map:view,alarm:view,jt:view,mediaServer:view,system:view'
WHERE id = 3;

-- 4. 用户-区域绑定（数据级权限：用户只能看到自己负责区域下的通道）
drop table IF EXISTS wvp_user_region;
create table IF NOT EXISTS wvp_user_region
(
    id          serial primary key COMMENT '主键ID',
    user_id     int          NOT NULL COMMENT '用户ID',
    region_id   int          NOT NULL COMMENT '区域节点ID（对应 wvp_common_region.id）',
    create_time varchar(50)  NOT NULL COMMENT '创建时间',
    constraint uk_user_region unique (user_id, region_id)
);

SELECT id, name, left(authority, 40) AS authority_head FROM wvp_user_role ORDER BY id;
