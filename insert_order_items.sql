USE supermarket;

-- =====================================================
-- Fix: Add order items for newly inserted orders
-- =====================================================

-- First, let's get the starting ID of newly inserted orders
SELECT MAX(id) INTO @max_order_id FROM `order`;

-- Calculate the base ID for new orders
SET @base_id = @max_order_id - 41;  -- 42 new orders

-- =====================================================
-- Day 1 (Yesterday) - Orders: @base_id to @base_id+6
-- =====================================================
INSERT INTO order_item (order_id, product_id, sku_id, sku_code, product_name, specification, unit, price, quantity, amount) VALUES
(@base_id, 1, NULL, 'P001', 'Coca Cola', '500ml', 'Bottle', 3.5, 4, 14.0),
(@base_id, 2, NULL, 'P002', 'Snacks', '100g', 'Bag', 8.5, 2, 17.0),
(@base_id, 3, NULL, 'P003', 'Bread', '500g', 'Pack', 12.8, 10, 128.0),
(@base_id, 4, NULL, 'P004', 'Milk', '1L', 'Box', 6.5, 1, 6.5),
(@base_id+1, 5, NULL, 'P005', 'Eggs', '10pcs', 'Pack', 15.0, 2, 30.0),
(@base_id+1, 6, NULL, 'P006', 'Rice', '5kg', 'Bag', 35.0, 1, 35.0),
(@base_id+1, 7, NULL, 'P007', 'Oil', '1.8L', 'Bottle', 45.0, 1, 45.0),
(@base_id+2, 8, NULL, 'P008', 'Salt', '500g', 'Bag', 3.5, 5, 17.5),
(@base_id+2, 9, NULL, 'P009', 'Soy Sauce', '500ml', 'Bottle', 8.5, 3, 25.5),
(@base_id+2, 10, NULL, 'P010', 'Vinegar', '500ml', 'Bottle', 6.5, 15, 97.5),
(@base_id+2, 11, NULL, 'P011', 'Tea', '200g', 'Pack', 35.0, 3, 105.0),
(@base_id+3, 12, NULL, 'P012', 'Coffee', '100g', 'Pack', 45.0, 1, 45.0),
(@base_id+3, 13, NULL, 'P013', 'Noodles', '500g', 'Pack', 8.0, 1, 8.0),
(@base_id+3, 14, NULL, 'P014', 'Cookies', '200g', 'Pack', 15.0, 1, 15.0),
(@base_id+4, 15, NULL, 'P015', 'Chocolate', '100g', 'Bar', 12.0, 8, 96.0),
(@base_id+4, 16, NULL, 'P016', 'Chips', '100g', 'Bag', 7.5, 11, 82.5),
(@base_id+5, 1, NULL, 'P001', 'Coca Cola', '500ml', 'Bottle', 3.5, 10, 35.0),
(@base_id+5, 2, NULL, 'P002', 'Snacks', '100g', 'Bag', 8.5, 1, 8.5),
(@base_id+6, 3, NULL, 'P003', 'Bread', '500g', 'Pack', 12.8, 20, 256.0),
(@base_id+6, 4, NULL, 'P004', 'Milk', '1L', 'Box', 6.5, 9, 58.5);

-- =====================================================
-- Day 2 - Orders: @base_id+7 to @base_id+12
-- =====================================================
INSERT INTO order_item (order_id, product_id, sku_id, sku_code, product_name, specification, unit, price, quantity, amount) VALUES
(@base_id+7, 5, NULL, 'P005', 'Eggs', '10pcs', 'Pack', 15.0, 4, 60.0),
(@base_id+7, 6, NULL, 'P006', 'Rice', '5kg', 'Bag', 35.0, 1, 35.0),
(@base_id+8, 7, NULL, 'P007', 'Oil', '1.8L', 'Bottle', 45.0, 3, 135.0),
(@base_id+8, 8, NULL, 'P008', 'Salt', '500g', 'Bag', 3.5, 2, 7.0),
(@base_id+8, 9, NULL, 'P009', 'Soy Sauce', '500ml', 'Bottle', 8.5, 1, 8.5),
(@base_id+9, 10, NULL, 'P010', 'Vinegar', '500ml', 'Bottle', 6.5, 5, 32.5),
(@base_id+9, 11, NULL, 'P011', 'Tea', '200g', 'Pack', 35.0, 2, 70.0),
(@base_id+10, 12, NULL, 'P012', 'Coffee', '100g', 'Pack', 45.0, 2, 90.0),
(@base_id+10, 13, NULL, 'P013', 'Noodles', '500g', 'Pack', 8.0, 4, 32.0),
(@base_id+11, 14, NULL, 'P014', 'Cookies', '200g', 'Pack', 15.0, 3, 45.0),
(@base_id+11, 15, NULL, 'P015', 'Chocolate', '100g', 'Bar', 12.0, 1, 12.0),
(@base_id+12, 16, NULL, 'P016', 'Chips', '100g', 'Bag', 7.5, 7, 52.5);

-- =====================================================
-- Day 3 - Orders: @base_id+13 to @base_id+19
-- =====================================================
INSERT INTO order_item (order_id, product_id, sku_id, sku_code, product_name, specification, unit, price, quantity, amount) VALUES
(@base_id+13, 1, NULL, 'P001', 'Coca Cola', '500ml', 'Bottle', 3.5, 5, 17.5),
(@base_id+13, 2, NULL, 'P002', 'Snacks', '100g', 'Bag', 8.5, 5, 42.5),
(@base_id+13, 3, NULL, 'P003', 'Bread', '500g', 'Pack', 12.8, 10, 128.0),
(@base_id+13, 4, NULL, 'P004', 'Milk', '1L', 'Box', 6.5, 1, 6.5),
(@base_id+14, 5, NULL, 'P005', 'Eggs', '10pcs', 'Pack', 15.0, 5, 75.0),
(@base_id+14, 6, NULL, 'P006', 'Rice', '5kg', 'Bag', 35.0, 1, 35.0),
(@base_id+15, 7, NULL, 'P007', 'Oil', '1.8L', 'Bottle', 45.0, 2, 90.0),
(@base_id+15, 8, NULL, 'P008', 'Salt', '500g', 'Bag', 3.5, 3, 10.5),
(@base_id+15, 9, NULL, 'P009', 'Soy Sauce', '500ml', 'Bottle', 8.5, 5, 42.5),
(@base_id+16, 10, NULL, 'P010', 'Vinegar', '500ml', 'Bottle', 6.5, 10, 65.0),
(@base_id+16, 11, NULL, 'P011', 'Tea', '200g', 'Pack', 35.0, 5, 175.0),
(@base_id+17, 12, NULL, 'P012', 'Coffee', '100g', 'Pack', 45.0, 1, 45.0),
(@base_id+17, 13, NULL, 'P013', 'Noodles', '500g', 'Pack', 8.0, 2, 16.0),
(@base_id+18, 14, NULL, 'P014', 'Cookies', '200g', 'Pack', 15.0, 2, 30.0),
(@base_id+18, 15, NULL, 'P015', 'Chocolate', '100g', 'Bar', 12.0, 1, 12.0),
(@base_id+19, 16, NULL, 'P016', 'Chips', '100g', 'Bag', 7.5, 20, 150.0);

-- =====================================================
-- Day 4 - Orders: @base_id+20 to @base_id+24
-- =====================================================
INSERT INTO order_item (order_id, product_id, sku_id, sku_code, product_name, specification, unit, price, quantity, amount) VALUES
(@base_id+20, 1, NULL, 'P001', 'Coca Cola', '500ml', 'Bottle', 3.5, 8, 28.0),
(@base_id+20, 2, NULL, 'P002', 'Snacks', '100g', 'Bag', 8.5, 6, 51.0),
(@base_id+20, 3, NULL, 'P003', 'Bread', '500g', 'Pack', 12.8, 6, 76.8),
(@base_id+20, 4, NULL, 'P004', 'Milk', '1L', 'Box', 6.5, 1, 6.5),
(@base_id+21, 5, NULL, 'P005', 'Eggs', '10pcs', 'Pack', 15.0, 5, 75.0),
(@base_id+21, 6, NULL, 'P006', 'Rice', '5kg', 'Bag', 35.0, 1, 35.0),
(@base_id+22, 7, NULL, 'P007', 'Oil', '1.8L', 'Bottle', 45.0, 3, 135.0),
(@base_id+22, 8, NULL, 'P008', 'Salt', '500g', 'Bag', 3.5, 4, 14.0),
(@base_id+22, 9, NULL, 'P009', 'Soy Sauce', '500ml', 'Bottle', 8.5, 4, 34.0),
(@base_id+23, 10, NULL, 'P010', 'Vinegar', '500ml', 'Bottle', 6.5, 6, 39.0),
(@base_id+23, 11, NULL, 'P011', 'Tea', '200g', 'Pack', 35.0, 3, 105.0),
(@base_id+24, 12, NULL, 'P012', 'Coffee', '100g', 'Pack', 45.0, 1, 45.0);

-- =====================================================
-- Day 5 - Orders: @base_id+25 to @base_id+30
-- =====================================================
INSERT INTO order_item (order_id, product_id, sku_id, sku_code, product_name, specification, unit, price, quantity, amount) VALUES
(@base_id+25, 1, NULL, 'P001', 'Coca Cola', '500ml', 'Bottle', 3.5, 6, 21.0),
(@base_id+25, 2, NULL, 'P002', 'Snacks', '100g', 'Bag', 8.5, 3, 25.5),
(@base_id+25, 3, NULL, 'P003', 'Bread', '500g', 'Pack', 12.8, 12, 153.6),
(@base_id+26, 4, NULL, 'P004', 'Milk', '1L', 'Box', 6.5, 3, 19.5),
(@base_id+26, 5, NULL, 'P005', 'Eggs', '10pcs', 'Pack', 15.0, 3, 45.0),
(@base_id+27, 6, NULL, 'P006', 'Rice', '5kg', 'Bag', 35.0, 2, 70.0),
(@base_id+27, 7, NULL, 'P007', 'Oil', '1.8L', 'Bottle', 45.0, 1, 45.0),
(@base_id+28, 8, NULL, 'P008', 'Salt', '500g', 'Bag', 3.5, 6, 21.0),
(@base_id+28, 9, NULL, 'P009', 'Soy Sauce', '500ml', 'Bottle', 8.5, 2, 17.0),
(@base_id+28, 10, NULL, 'P010', 'Vinegar', '500ml', 'Bottle', 6.5, 8, 52.0),
(@base_id+29, 11, NULL, 'P011', 'Tea', '200g', 'Pack', 35.0, 6, 210.0),
(@base_id+30, 12, NULL, 'P012', 'Coffee', '100g', 'Pack', 45.0, 1, 45.0);

-- =====================================================
-- Day 6 - Orders: @base_id+31 to @base_id+35
-- =====================================================
INSERT INTO order_item (order_id, product_id, sku_id, sku_code, product_name, specification, unit, price, quantity, amount) VALUES
(@base_id+31, 1, NULL, 'P001', 'Coca Cola', '500ml', 'Bottle', 3.5, 10, 35.0),
(@base_id+31, 2, NULL, 'P002', 'Snacks', '100g', 'Bag', 8.5, 8, 68.0),
(@base_id+31, 3, NULL, 'P003', 'Bread', '500g', 'Pack', 12.8, 7, 89.6),
(@base_id+32, 4, NULL, 'P004', 'Milk', '1L', 'Box', 6.5, 5, 32.5),
(@base_id+32, 5, NULL, 'P005', 'Eggs', '10pcs', 'Pack', 15.0, 3, 45.0),
(@base_id+33, 6, NULL, 'P006', 'Rice', '5kg', 'Bag', 35.0, 1, 35.0),
(@base_id+33, 7, NULL, 'P007', 'Oil', '1.8L', 'Bottle', 45.0, 2, 90.0),
(@base_id+34, 8, NULL, 'P008', 'Salt', '500g', 'Bag', 3.5, 5, 17.5),
(@base_id+34, 9, NULL, 'P009', 'Soy Sauce', '500ml', 'Bottle', 8.5, 3, 25.5),
(@base_id+35, 10, NULL, 'P010', 'Vinegar', '500ml', 'Bottle', 6.5, 12, 78.0);

-- =====================================================
-- Day 7 - Orders: @base_id+36 to @base_id+41
-- =====================================================
INSERT INTO order_item (order_id, product_id, sku_id, sku_code, product_name, specification, unit, price, quantity, amount) VALUES
(@base_id+36, 1, NULL, 'P001', 'Coca Cola', '500ml', 'Bottle', 3.5, 7, 24.5),
(@base_id+36, 2, NULL, 'P002', 'Snacks', '100g', 'Bag', 8.5, 4, 34.0),
(@base_id+36, 3, NULL, 'P003', 'Bread', '500g', 'Pack', 12.8, 8, 102.4),
(@base_id+37, 4, NULL, 'P004', 'Milk', '1L', 'Box', 6.5, 2, 13.0),
(@base_id+37, 5, NULL, 'P005', 'Eggs', '10pcs', 'Pack', 15.0, 4, 60.0),
(@base_id+38, 6, NULL, 'P006', 'Rice', '5kg', 'Bag', 35.0, 3, 105.0),
(@base_id+38, 7, NULL, 'P007', 'Oil', '1.8L', 'Bottle', 45.0, 1, 45.0),
(@base_id+39, 8, NULL, 'P008', 'Salt', '500g', 'Bag', 3.5, 4, 14.0),
(@base_id+39, 9, NULL, 'P009', 'Soy Sauce', '500ml', 'Bottle', 8.5, 2, 17.0),
(@base_id+39, 10, NULL, 'P010', 'Vinegar', '500ml', 'Bottle', 6.5, 6, 39.0),
(@base_id+40, 11, NULL, 'P011', 'Tea', '200g', 'Pack', 35.0, 4, 140.0),
(@base_id+41, 12, NULL, 'P012', 'Coffee', '100g', 'Pack', 45.0, 2, 90.0);

-- Verify
SELECT 'New order items inserted:', COUNT(*) FROM order_item WHERE order_id >= @base_id;