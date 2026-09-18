package com.supermarket.backend.mapper;

import com.supermarket.backend.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface PaymentRecordMapper {

    PaymentRecord selectById(@Param("id") Long id);

    PaymentRecord selectByPayNo(@Param("payNo") String payNo);

    List<PaymentRecord> selectByOrderId(@Param("orderId") Long orderId);

    /** 查询该订单当前进行中的支付单（PENDING），用于复用未完成的支付 */
    PaymentRecord selectPendingByOrderId(@Param("orderId") Long orderId);

    /** 查询该订单已成功的支付单 */
    PaymentRecord selectSuccessByOrderId(@Param("orderId") Long orderId);

    int insert(PaymentRecord record);

    int update(PaymentRecord record);

    /**
     * 幂等置为成功：仅当该支付单仍为 PENDING 时更新（防重复回调重复处理）。
     *
     * @return 受影响行数，0 表示已被处理过
     */
    int markSuccessIfPending(@Param("payNo") String payNo,
                             @Param("transactionId") String transactionId,
                             @Param("payTime") Date payTime);

    /** 幂等关闭：仅当仍为 PENDING 时置为 CLOSED */
    int closeIfPending(@Param("payNo") String payNo, @Param("updateTime") Date updateTime);

    /** 关闭某订单下全部进行中的支付单（订单超时取消时调用） */
    int closePendingByOrderId(@Param("orderId") Long orderId, @Param("updateTime") Date updateTime);
}
