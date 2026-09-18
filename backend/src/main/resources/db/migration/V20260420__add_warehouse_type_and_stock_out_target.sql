-- 添加仓库类型字段
ALTER TABLE warehouse ADD COLUMN type INT DEFAULT 1 COMMENT '仓库类型（1-总仓，2-门店仓，3-退货仓）' AFTER name;

-- 添加出库目标和目标仓库字段
ALTER TABLE stock_out_record ADD COLUMN out_target VARCHAR(50) DEFAULT NULL COMMENT '出库目标' AFTER out_type;
ALTER TABLE stock_out_record ADD COLUMN target_warehouse_id BIGINT DEFAULT NULL COMMENT '目标仓库ID（调拨时使用）' AFTER out_target;
