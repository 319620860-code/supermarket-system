USE supermarket;

-- =====================================================
-- Insert simulation data for last 7 days
-- =====================================================

-- Delete old test data
DELETE FROM order_item WHERE order_id > 0;
DELETE FROM `order` WHERE create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY);

-- Set date range (last 7 days)
SET @day1 = DATE_SUB(CURDATE(), INTERVAL 1 DAY);
SET @day2 = DATE_SUB(CURDATE(), INTERVAL 2 DAY);
SET @day3 = DATE_SUB(CURDATE(), INTERVAL 3 DAY);
SET @day4 = DATE_SUB(CURDATE(), INTERVAL 4 DAY);
SET @day5 = DATE_SUB(CURDATE(), INTERVAL 5 DAY);
SET @day6 = DATE_SUB(CURDATE(), INTERVAL 6 DAY);
SET @day7 = DATE_SUB(CURDATE(), INTERVAL 7 DAY);

-- =====================================================
-- Day 1 (Yesterday)
-- =====================================================
INSERT INTO `order` (order_number, customer_name, customer_phone, cashier_id, cashier_name, operator_id, total_amount, discount_amount, actual_amount, payment_method, payment_status, order_status, remark, create_time, update_time) VALUES
('ORD20260518001', 'Zhang San', '13800138001', '1', 'Admin', 1, 156.80, 0.00, 156.80, 'WeChat', 'PAID', 'COMPLETED', '', CONCAT(@day1, ' 09:15:23'), CONCAT(@day1, ' 09:15:23')),
('ORD20260518002', 'Li Si', '13800138002', '1', 'Admin', 1, 89.50, 5.00, 84.50, 'Alipay', 'PAID', 'COMPLETED', 'Member Discount', CONCAT(@day1, ' 10:32:11'), CONCAT(@day1, ' 10:32:11')),
('ORD20260518003', 'Wang Wu', '13800138003', '1', 'Admin', 1, 234.00, 0.00, 234.00, 'Cash', 'PAID', 'COMPLETED', '', CONCAT(@day1, ' 11:45:00'), CONCAT(@day1, ' 11:45:00')),
('ORD20260518004', 'Zhao Liu', '13800138004', '1', 'Admin', 1, 67.20, 0.00, 67.20, 'WeChat', 'PAID', 'COMPLETED', '', CONCAT(@day1, ' 14:20:35'), CONCAT(@day1, ' 14:20:35')),
('ORD20260518005', 'Sun Qi', '13800138005', '1', 'Admin', 1, 189.00, 10.00, 179.00, 'Alipay', 'PAID', 'COMPLETED', 'Promotion', CONCAT(@day1, ' 16:55:18'), CONCAT(@day1, ' 16:55:18')),
('ORD20260518006', 'Zhou Ba', '13800138006', '1', 'Admin', 1, 45.60, 0.00, 45.60, 'WeChat', 'PAID', 'COMPLETED', '', CONCAT(@day1, ' 18:30:42'), CONCAT(@day1, ' 18:30:42')),
('ORD20260518007', 'Wu Jiu', '13800138007', '1', 'Admin', 1, 312.50, 0.00, 312.50, 'Alipay', 'PAID', 'COMPLETED', '', CONCAT(@day1, ' 19:15:00'), CONCAT(@day1, ' 19:15:00'));

-- =====================================================
-- Day 2
-- =====================================================
INSERT INTO `order` (order_number, customer_name, customer_phone, cashier_id, cashier_name, operator_id, total_amount, discount_amount, actual_amount, payment_method, payment_status, order_status, remark, create_time, update_time) VALUES
('ORD20260517001', 'Qian Yi', '13900139001', '1', 'Admin', 1, 78.90, 0.00, 78.90, 'WeChat', 'PAID', 'COMPLETED', '', CONCAT(@day2, ' 08:45:12'), CONCAT(@day2, ' 08:45:12')),
('ORD20260517002', 'Chen Er', '13900139002', '1', 'Admin', 1, 156.00, 0.00, 156.00, 'Cash', 'PAID', 'COMPLETED', '', CONCAT(@day2, ' 10:20:33'), CONCAT(@day2, ' 10:20:33')),
('ORD20260517003', 'Liu San', '13900139003', '1', 'Admin', 1, 89.50, 5.00, 84.50, 'Alipay', 'PAID', 'COMPLETED', 'Member Discount', CONCAT(@day2, ' 12:15:45'), CONCAT(@day2, ' 12:15:45')),
('ORD20260517004', 'Yang Si', '13900139004', '1', 'Admin', 1, 245.60, 0.00, 245.60, 'WeChat', 'PAID', 'COMPLETED', '', CONCAT(@day2, ' 15:30:20'), CONCAT(@day2, ' 15:30:20')),
('ORD20260517005', 'Huang Wu', '13900139005', '1', 'Admin', 1, 134.00, 0.00, 134.00, 'Alipay', 'PAID', 'COMPLETED', '', CONCAT(@day2, ' 17:45:11'), CONCAT(@day2, ' 17:45:11')),
('ORD20260517006', 'Lin Liu', '13900139006', '1', 'Admin', 1, 56.80, 0.00, 56.80, 'WeChat', 'PAID', 'COMPLETED', '', CONCAT(@day2, ' 19:00:55'), CONCAT(@day2, ' 19:00:55'));

-- =====================================================
-- Day 3
-- =====================================================
INSERT INTO `order` (order_number, customer_name, customer_phone, cashier_id, cashier_name, operator_id, total_amount, discount_amount, actual_amount, payment_method, payment_status, order_status, remark, create_time, update_time) VALUES
('ORD20260516001', 'He Qi', '13700137001', '1', 'Admin', 1, 198.00, 0.00, 198.00, 'WeChat', 'PAID', 'COMPLETED', '', CONCAT(@day3, ' 09:00:00'), CONCAT(@day3, ' 09:00:00')),
('ORD20260516002', 'Gao Ba', '13700137002', '1', 'Admin', 1, 87.50, 0.00, 87.50, 'Alipay', 'PAID', 'COMPLETED', '', CONCAT(@day3, ' 11:20:15'), CONCAT(@day3, ' 11:20:15')),
('ORD20260516003', 'Ma Jiu', '13700137003', '1', 'Admin', 1, 156.80, 10.00, 146.80, 'Cash', 'PAID', 'COMPLETED', 'Promotion', CONCAT(@day3, ' 13:45:30'), CONCAT(@day3, ' 13:45:30')),
('ORD20260516004', 'Tang Shi', '13700137004', '1', 'Admin', 1, 234.00, 0.00, 234.00, 'WeChat', 'PAID', 'COMPLETED', '', CONCAT(@day3, ' 16:10:22'), CONCAT(@day3, ' 16:10:22')),
('ORD20260516005', 'Han Yi', '13700137005', '1', 'Admin', 1, 78.90, 0.00, 78.90, 'Alipay', 'PAID', 'COMPLETED', '', CONCAT(@day3, ' 18:55:40'), CONCAT(@day3, ' 18:55:40')),
('ORD20260516006', 'Feng Er', '13700137006', '1', 'Admin', 1, 189.00, 0.00, 189.00, 'WeChat', 'PAID', 'COMPLETED', '', CONCAT(@day3, ' 19:30:00'), CONCAT(@day3, ' 19:30:00')),
('ORD20260516007', 'Cao San', '13700137007', '1', 'Admin', 1, 45.60, 0.00, 45.60, 'Cash', 'PAID', 'COMPLETED', '', CONCAT(@day3, ' 20:15:18'), CONCAT(@day3, ' 20:15:18'));

-- =====================================================
-- Day 4
-- =====================================================
INSERT INTO `order` (order_number, customer_name, customer_phone, cashier_id, cashier_name, operator_id, total_amount, discount_amount, actual_amount, payment_method, payment_status, order_status, remark, create_time, update_time) VALUES
('ORD20260515001', 'Xu Si', '13600136001', '1', 'Admin', 1, 167.00, 0.00, 167.00, 'WeChat', 'PAID', 'COMPLETED', '', CONCAT(@day4, ' 08:30:00'), CONCAT(@day4, ' 08:30:00')),
('ORD20260515002', 'Su Wu', '13600136002', '1', 'Admin', 1, 89.00, 0.00, 89.00, 'Alipay', 'PAID', 'COMPLETED', '', CONCAT(@day4, ' 10:45:22'), CONCAT(@day4, ' 10:45:22')),
('ORD20260515003', 'Pan Liu', '13600136003', '1', 'Admin', 1, 234.50, 15.00, 219.50, 'WeChat', 'PAID', 'COMPLETED', 'Promotion', CONCAT(@day4, ' 12:20:10'), CONCAT(@day4, ' 12:20:10')),
('ORD20260515004', 'Ge Qi', '13600136004', '1', 'Admin', 1, 156.80, 0.00, 156.80, 'Cash', 'PAID', 'COMPLETED', '', CONCAT(@day4, ' 15:00:33'), CONCAT(@day4, ' 15:00:33')),
('ORD20260515005', 'Lv Ba', '13600136005', '1', 'Admin', 1, 78.90, 0.00, 78.90, 'Alipay', 'PAID', 'COMPLETED', '', CONCAT(@day4, ' 17:30:45'), CONCAT(@day4, ' 17:30:45'));

-- =====================================================
-- Day 5
-- =====================================================
INSERT INTO `order` (order_number, customer_name, customer_phone, cashier_id, cashier_name, operator_id, total_amount, discount_amount, actual_amount, payment_method, payment_status, order_status, remark, create_time, update_time) VALUES
('ORD20260514001', 'Shi Jiu', '13500135001', '1', 'Admin', 1, 198.50, 0.00, 198.50, 'WeChat', 'PAID', 'COMPLETED', '', CONCAT(@day5, ' 09:15:00'), CONCAT(@day5, ' 09:15:00')),
('ORD20260514002', 'Zhang Shi', '13500135002', '1', 'Admin', 1, 67.80, 0.00, 67.80, 'Alipay', 'PAID', 'COMPLETED', '', CONCAT(@day5, ' 11:00:20'), CONCAT(@day5, ' 11:00:20')),
('ORD20260514003', 'Kong Yi', '13500135003', '1', 'Admin', 1, 145.00, 5.00, 140.00, 'WeChat', 'PAID', 'COMPLETED', 'Member Discount', CONCAT(@day5, ' 14:30:15'), CONCAT(@day5, ' 14:30:15')),
('ORD20260514004', 'Cao Er', '13500135004', '1', 'Admin', 1, 89.00, 0.00, 89.00, 'Cash', 'PAID', 'COMPLETED', '', CONCAT(@day5, ' 16:45:30'), CONCAT(@day5, ' 16:45:30')),
('ORD20260514005', 'Yan San', '13500135005', '1', 'Admin', 1, 267.00, 0.00, 267.00, 'WeChat', 'PAID', 'COMPLETED', '', CONCAT(@day5, ' 19:20:00'), CONCAT(@day5, ' 19:20:00')),
('ORD20260514006', 'Hua Si', '13500135006', '1', 'Admin', 1, 123.50, 0.00, 123.50, 'Alipay', 'PAID', 'COMPLETED', '', CONCAT(@day5, ' 20:00:25'), CONCAT(@day5, ' 20:00:25'));

-- =====================================================
-- Day 6
-- =====================================================
INSERT INTO `order` (order_number, customer_name, customer_phone, cashier_id, cashier_name, operator_id, total_amount, discount_amount, actual_amount, payment_method, payment_status, order_status, remark, create_time, update_time) VALUES
('ORD20260513001', 'Jin Wu', '13400134001', '1', 'Admin', 1, 189.00, 0.00, 189.00, 'WeChat', 'PAID', 'COMPLETED', '', CONCAT(@day6, ' 08:00:00'), CONCAT(@day6, ' 08:00:00')),
('ORD20260513002', 'Wei Liu', '13400134002', '1', 'Admin', 1, 78.50, 0.00, 78.50, 'Alipay', 'PAID', 'COMPLETED', '', CONCAT(@day6, ' 10:30:15'), CONCAT(@day6, ' 10:30:15')),
('ORD20260513003', 'Tao Qi', '13400134003', '1', 'Admin', 1, 156.00, 10.00, 146.00, 'WeChat', 'PAID', 'COMPLETED', 'Promotion', CONCAT(@day6, ' 13:15:45'), CONCAT(@day6, ' 13:15:45')),
('ORD20260513004', 'Jiang Ba', '13400134004', '1', 'Admin', 1, 89.90, 0.00, 89.90, 'Cash', 'PAID', 'COMPLETED', '', CONCAT(@day6, ' 16:00:30'), CONCAT(@day6, ' 16:00:30')),
('ORD20260513005', 'Qi Jiu', '13400134005', '1', 'Admin', 1, 234.50, 0.00, 234.50, 'WeChat', 'PAID', 'COMPLETED', '', CONCAT(@day6, ' 18:30:20'), CONCAT(@day6, ' 18:30:20'));

-- =====================================================
-- Day 7
-- =====================================================
INSERT INTO `order` (order_number, customer_name, customer_phone, cashier_id, cashier_name, operator_id, total_amount, discount_amount, actual_amount, payment_method, payment_status, order_status, remark, create_time, update_time) VALUES
('ORD20260512001', 'Xie Shi', '13300133001', '1', 'Admin', 1, 167.80, 0.00, 167.80, 'WeChat', 'PAID', 'COMPLETED', '', CONCAT(@day7, ' 09:00:00'), CONCAT(@day7, ' 09:00:00')),
('ORD20260512002', 'Zou Yi', '13300133002', '1', 'Admin', 1, 89.00, 5.00, 84.00, 'Alipay', 'PAID', 'COMPLETED', 'Member Discount', CONCAT(@day7, ' 11:30:22'), CONCAT(@day7, ' 11:30:22')),
('ORD20260512003', 'Bai Er', '13300133003', '1', 'Admin', 1, 198.50, 0.00, 198.50, 'WeChat', 'PAID', 'COMPLETED', '', CONCAT(@day7, ' 14:15:10'), CONCAT(@day7, ' 14:15:10')),
('ORD20260512004', 'Shui San', '13300133004', '1', 'Admin', 1, 67.50, 0.00, 67.50, 'Cash', 'PAID', 'COMPLETED', '', CONCAT(@day7, ' 17:00:45'), CONCAT(@day7, ' 17:00:45')),
('ORD20260512005', 'Dou Si', '13300133005', '1', 'Admin', 1, 245.00, 0.00, 245.00, 'Alipay', 'PAID', 'COMPLETED', '', CONCAT(@day7, ' 19:45:30'), CONCAT(@day7, ' 19:45:30')),
('ORD20260512006', 'Zhang Wu', '13300133006', '1', 'Admin', 1, 123.00, 0.00, 123.00, 'WeChat', 'PAID', 'COMPLETED', '', CONCAT(@day7, ' 20:30:00'), CONCAT(@day7, ' 20:30:00'));

-- =====================================================
-- Insert Order Items
-- =====================================================
INSERT INTO order_item (order_id, product_id, sku_id, sku_code, product_name, specification, unit, price, quantity, amount) VALUES
(1, 1, NULL, 'P001', 'Coca Cola', '500ml', 'Bottle', 3.5, 2, 7.0),
(1, 2, NULL, 'P002', 'Snacks', '100g', 'Bag', 8.5, 1, 8.5),
(1, 3, NULL, 'P003', 'Bread', '500g', 'Pack', 12.8, 1, 12.8),
(2, 4, NULL, 'P004', 'Milk', '1L', 'Box', 6.5, 2, 13.0),
(2, 5, NULL, 'P005', 'Eggs', '10pcs', 'Pack', 15.0, 1, 15.0),
(3, 6, NULL, 'P006', 'Rice', '5kg', 'Bag', 35.0, 1, 35.0),
(3, 7, NULL, 'P007', 'Oil', '1.8L', 'Bottle', 45.0, 1, 45.0),
(3, 8, NULL, 'P008', 'Salt', '500g', 'Bag', 3.5, 2, 7.0),
(4, 9, NULL, 'P009', 'Soy Sauce', '500ml', 'Bottle', 8.5, 1, 8.5),
(4, 10, NULL, 'P010', 'Vinegar', '500ml', 'Bottle', 6.5, 2, 13.0),
(5, 11, NULL, 'P011', 'Tea', '200g', 'Pack', 35.0, 1, 35.0),
(5, 12, NULL, 'P012', 'Coffee', '100g', 'Pack', 45.0, 1, 45.0),
(6, 13, NULL, 'P013', 'Noodles', '500g', 'Pack', 8.0, 2, 16.0),
(6, 14, NULL, 'P014', 'Cookies', '200g', 'Pack', 15.0, 1, 15.0),
(7, 15, NULL, 'P015', 'Chocolate', '100g', 'Bar', 12.0, 2, 24.0),
(7, 16, NULL, 'P016', 'Chips', '100g', 'Bag', 7.5, 3, 22.5),
(8, 1, NULL, 'P001', 'Coca Cola', '500ml', 'Bottle', 3.5, 1, 3.5),
(8, 2, NULL, 'P002', 'Snacks', '100g', 'Bag', 8.5, 2, 17.0),
(9, 3, NULL, 'P003', 'Bread', '500g', 'Pack', 12.8, 1, 12.8),
(9, 4, NULL, 'P004', 'Milk', '1L', 'Box', 6.5, 3, 19.5),
(10, 5, NULL, 'P005', 'Eggs', '10pcs', 'Pack', 15.0, 1, 15.0),
(10, 6, NULL, 'P006', 'Rice', '5kg', 'Bag', 35.0, 1, 35.0),
(11, 7, NULL, 'P007', 'Oil', '1.8L', 'Bottle', 45.0, 1, 45.0),
(11, 8, NULL, 'P008', 'Salt', '500g', 'Bag', 3.5, 1, 3.5),
(12, 9, NULL, 'P009', 'Soy Sauce', '500ml', 'Bottle', 8.5, 2, 17.0),
(12, 10, NULL, 'P010', 'Vinegar', '500ml', 'Bottle', 6.5, 1, 6.5),
(13, 11, NULL, 'P011', 'Tea', '200g', 'Pack', 35.0, 2, 70.0),
(13, 12, NULL, 'P012', 'Coffee', '100g', 'Pack', 45.0, 1, 45.0),
(14, 13, NULL, 'P013', 'Noodles', '500g', 'Pack', 8.0, 1, 8.0),
(14, 14, NULL, 'P014', 'Cookies', '200g', 'Pack', 15.0, 2, 30.0),
(15, 15, NULL, 'P015', 'Chocolate', '100g', 'Bar', 12.0, 1, 12.0),
(15, 16, NULL, 'P016', 'Chips', '100g', 'Bag', 7.5, 1, 7.5),
(16, 1, NULL, 'P001', 'Coca Cola', '500ml', 'Bottle', 3.5, 3, 10.5),
(16, 2, NULL, 'P002', 'Snacks', '100g', 'Bag', 8.5, 1, 8.5),
(17, 3, NULL, 'P003', 'Bread', '500g', 'Pack', 12.8, 2, 25.6),
(17, 4, NULL, 'P004', 'Milk', '1L', 'Box', 6.5, 1, 6.5),
(18, 5, NULL, 'P005', 'Eggs', '10pcs', 'Pack', 15.0, 2, 30.0),
(18, 6, NULL, 'P006', 'Rice', '5kg', 'Bag', 35.0, 1, 35.0),
(19, 7, NULL, 'P007', 'Oil', '1.8L', 'Bottle', 45.0, 1, 45.0),
(19, 8, NULL, 'P008', 'Salt', '500g', 'Bag', 3.5, 3, 10.5),
(20, 9, NULL, 'P009', 'Soy Sauce', '500ml', 'Bottle', 8.5, 1, 8.5),
(20, 10, NULL, 'P010', 'Vinegar', '500ml', 'Bottle', 6.5, 2, 13.0);

-- Verify
SELECT DATE(create_time) as order_date, COUNT(*) as order_count, SUM(actual_amount) as total_amount
FROM `order`
WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY DATE(create_time)
ORDER BY order_date DESC;

SELECT 'Total:', COUNT(*), SUM(actual_amount) FROM `order`;