USE supermarket;
-- Check the order IDs of newly inserted orders
SELECT id, order_number, create_time FROM `order` WHERE create_time >= '2026-05-01' ORDER BY id LIMIT 5;

-- Check if order_items are correctly associated
SELECT oi.order_id, o.order_number, COUNT(*) as item_count
FROM order_item oi
JOIN `order` o ON oi.order_id = o.id
WHERE o.create_time >= '2026-05-01'
GROUP BY oi.order_id, o.order_number
ORDER BY oi.order_id
LIMIT 10;