package com.supermarket.backend.mapper;

import com.supermarket.backend.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 根据ID查询订单
     * @param id 订单ID
     * @return 订单
     */
    Order selectById(@Param("id") Long id);

    /**
     * 根据订单号查询订单
     * @param orderNumber 订单号
     * @return 订单
     */
    Order selectByOrderNumber(@Param("orderNumber") String orderNumber);

    /**
     * 分页查询订单列表
     * @param params 查询参数
     * @return 订单列表
     */
    List<Order> selectPage(Map<String, Object> params);

    /**
     * 查询订单总数
     * @param params 查询参数
     * @return 订单总数
     */
    Integer selectCount(Map<String, Object> params);

    /**
     * 插入订单
     * @param order 订单
     * @return 插入成功的记录数
     */
    int insert(Order order);

    /**
     * 更新订单
     * @param order 订单
     * @return 更新成功的记录数
     */
    int update(Order order);

    /**
     * 删除订单
     * @param id 订单ID
     * @return 删除成功的记录数
     */
    int delete(@Param("id") Long id);

    /**
     * 根据收银员ID查询订单
     * @param cashierId 收银员ID
     * @return 订单列表
     */
    List<Order> selectByCashierId(@Param("cashierId") String cashierId);

    /**
     * 查询今日销售额
     * @return 今日销售额
     */
    Double selectTodaySales();

    /**
     * 查询指定日期范围的销售额
     * @param params 查询参数
     * @return 销售额
     */
    Double selectSalesByDateRange(Map<String, Object> params);

    /**
     * 查询指定日期范围的订单数量
     * @param params 查询参数
     * @return 订单数量
     */
    Integer selectOrderCountByDateRange(Map<String, Object> params);

    /**
     * 条件取消（幂等）：仅当订单仍为「payment_status=UNPAID 且 order_status=CREATED」时置为 CANCELLED。
     * 依赖数据库行锁保证并发下只有一个调用方更新成功，用于超时自动取消与手工取消的幂等控制。
     *
     * @param id         订单ID
     * @param updateTime 更新时间
     * @return 受影响行数（0 表示订单状态已变更，调用方应放弃后续库存操作）
     */
    int cancelIfUnpaid(@Param("id") Long id, @Param("updateTime") Date updateTime);

    /**
     * 条件支付（幂等）：仅当订单仍为「payment_status=UNPAID 且 order_status=CREATED」时置为 PAID/COMPLETED。
     *
     * @param id            订单ID
     * @param paymentMethod 支付方式
     * @param actualAmount  实付金额
     * @param remark        备注
     * @param updateTime    更新时间
     * @return 受影响行数（0 表示已被支付或已取消）
     */
    int payIfUnpaid(@Param("id") Long id,
                    @Param("paymentMethod") String paymentMethod,
                    @Param("actualAmount") Double actualAmount,
                    @Param("remark") String remark,
                    @Param("updateTime") Date updateTime);

    /**
     * 查询已超过指定时间仍未支付的订单（兜底扫描任务使用）。
     *
     * @param deadline 超时截止时间（create_time &lt;= deadline 视为超时）
     * @param limit    单批最大条数
     * @return 超时未支付订单列表
     */
    List<Order> selectTimeoutUnpaid(@Param("deadline") Date deadline, @Param("limit") int limit);
}
