package com.genersoft.iot.vmp.service.impl;

import com.genersoft.iot.vmp.service.IUserRegionService;
import com.genersoft.iot.vmp.storager.dao.UserRegionMapper;
import com.genersoft.iot.vmp.storager.dao.dto.UserRegion;
import com.genersoft.iot.vmp.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户-区域绑定服务实现
 */
@Service
public class UserRegionServiceImpl implements IUserRegionService {

    @Autowired
    private UserRegionMapper userRegionMapper;

    @Override
    public List<Integer> getRegionIdsByUserId(int userId) {
        return userRegionMapper.queryRegionIdsByUserId(userId);
    }

    @Override
    @Transactional
    public void saveUserRegions(int userId, List<Integer> regionIds) {
        // 先删除原有绑定
        userRegionMapper.deleteByUserId(userId);
        if (regionIds == null || regionIds.isEmpty()) {
            return;
        }
        String now = DateUtil.getNow();
        List<UserRegion> userRegionList = new ArrayList<>(regionIds.size());
        for (Integer regionId : regionIds) {
            if (regionId == null) {
                continue;
            }
            UserRegion userRegion = new UserRegion();
            userRegion.setUserId(userId);
            userRegion.setRegionId(regionId);
            userRegion.setCreateTime(now);
            userRegionList.add(userRegion);
        }
        if (!userRegionList.isEmpty()) {
            userRegionMapper.batchAdd(userRegionList);
        }
    }
}
