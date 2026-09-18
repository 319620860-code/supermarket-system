package com.supermarket.backend.mapper;

import com.supermarket.backend.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface WarehouseMapper {
    /**
     * 根据ID查询仓库
     * @param id 仓库ID
     * @return 仓库信息
     */
    Warehouse selectById(@Param("id") Long id);

    /**
     * 分页查询仓库列表
     * @param params 查询参数
     * @return 仓库列表
     */
    List<Warehouse> selectPage(Map<String, Object> params);

    /**
     * 查询仓库总数
     * @param params 查询参数
     * @return 仓库总数
     */
    int selectCount(Map<String, Object> params);

    /**
     * 插入仓库
     * @param warehouse 仓库信息
     * @return 插入成功的记录数
     */
    int insert(Warehouse warehouse);

    /**
     * 更新仓库
     * @param warehouse 仓库信息
     * @return 更新成功的记录数
     */
    int update(Warehouse warehouse);

    /**
     * 删除仓库
     * @param id 仓库ID
     * @return 删除成功的记录数
     */
    int delete(@Param("id") Long id);

    /**
     * 获取所有仓库列表
     * @return 仓库列表
     */
    List<Warehouse> selectAll();
}