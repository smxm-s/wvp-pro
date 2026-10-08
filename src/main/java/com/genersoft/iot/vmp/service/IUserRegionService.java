package com.genersoft.iot.vmp.service;

import java.util.List;

/**
 * 用户-区域绑定服务
 */
public interface IUserRegionService {

    /**
     * 查询用户绑定的区域ID列表
     */
    List<Integer> getRegionIdsByUserId(int userId);

    /**
     * 保存用户绑定的区域（先删后插）
     */
    void saveUserRegions(int userId, List<Integer> regionIds);
}
