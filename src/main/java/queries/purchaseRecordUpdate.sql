UPDATE pharmacymanagement.purchase_record
SET purchase_date_time = DATE_ADD(purchase_date_time, INTERVAL 1 YEAR)
WHERE purchase_date_time LIKE '2024-%';