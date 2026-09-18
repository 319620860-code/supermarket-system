USE supermarket;
SELECT id, order_number, create_time FROM `order` WHERE create_time >= '2026-05-01' LIMIT 5;