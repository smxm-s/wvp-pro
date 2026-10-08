package com.genersoft.iot.vmp.storager.dao.dto;

/**
 * 用户-区域绑定关系
 */
public class UserRegion {

    private int id;

    /**
     * 用户ID
     */
    private int userId;

    /**
     * 区域ID（对应 wvp_common_region.id）
     */
    private int regionId;

    /**
     * 创建时间
     */
    private String createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRegionId() {
        return regionId;
    }

    public void setRegionId(int regionId) {
        this.regionId = regionId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
