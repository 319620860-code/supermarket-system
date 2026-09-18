USE supermarket;
SELECT
    oi.product_id AS product_id,
    p.name AS product_name,
    SUM(oi.quantity) AS total_quantity,
    SUM(oi.amount) AS salesAmount
FROM order_item oi
INNER JOIN product p ON oi.product_id = p.id
WHERE oi.order_id IN (
    SELECT id FROM `order`
    WHERE order_status = 'COMPLETED'
    AND payment_status = 'PAID'
    AND create_time BETWEEN '2026-05-01' AND '2026-05-21'
)
GROUP BY oi.product_id, p.name
ORDER BY salesAmount DESC
LIMIT 10;