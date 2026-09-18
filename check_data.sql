USE supermarket;
SELECT DATE(create_time) as date, COUNT(*) as count FROM `order` GROUP BY DATE(create_time) ORDER BY date DESC;
SELECT 'Total orders:', COUNT(*) FROM `order`;