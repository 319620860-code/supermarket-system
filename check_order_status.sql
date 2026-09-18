USE supermarket;
SELECT order_status, payment_status, COUNT(*) FROM `order` WHERE create_time >= '2026-05-01' GROUP BY order_status, payment_status;