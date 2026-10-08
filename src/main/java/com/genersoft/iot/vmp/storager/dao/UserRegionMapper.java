package com.genersoft.iot.vmp.storager.dao;

import com.genersoft.iot.vmp.storager.dao.dto.UserRegion;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户-区域绑定关系持久层
 */
@Mapper
@Repository
public interface UserRegionMapper {

    /**
     * 查询用户绑定的区域ID列表
     */
    @Select("select region_id from wvp_user_region where user_id = #{userId} order by id")
    List<Integer> queryRegionIdsByUserId(@Param("userId") int userId);

    /**
     * 删除用户的全部区域绑定
     */
    @Delete("delete from wvp_user_region where user_id = #{userId}")
    int deleteByUserId(@Param("userId") int userId);

    /**
     * 批量插入用户的区域绑定
     */
    @Insert(" <script>" +
            " INSERT INTO wvp_user_region (user_id, region_id, create_time) VALUES " +
            " <foreach collection='userRegionList' index='index' item='item' separator=','> " +
            " (#{item.userId}, #{item.regionId}, #{item.createTime})" +
            " </foreach> " +
            " </script>")
    int batchAdd(List<UserRegion> userRegionList);
}
