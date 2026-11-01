INSERT INTO suppliers (company_name, contact_name, email, phone, city, status) VALUES
('Green Basket Farms','Ananya Mehta','ananya@greenbasket.example','+91 98765 11001','Jaipur','ACTIVE'),
('Sunrise Dairy Co.','Rohit Verma','rohit@sunrisedairy.example','+91 98765 11002','Ajmer','ACTIVE'),
('Harvest Grain Traders','Neha Sharma','neha@harvestgrain.example','+91 98765 11003','Kota','ACTIVE');

INSERT INTO products (sku,name,category,quantity,reorder_level,unit_price,expiry_date,supplier_id) VALUES
('VEG-001','Fresh Tomatoes','Vegetables',42,15,32.50,DATE_ADD(CURDATE(), INTERVAL 5 DAY),1),
('VEG-002','Spinach Bundle','Vegetables',8,12,18.00,DATE_ADD(CURDATE(), INTERVAL 3 DAY),1),
('DAI-001','Full Cream Milk 1L','Dairy',24,10,68.00,DATE_ADD(CURDATE(), INTERVAL 7 DAY),2),
('GRA-001','Basmati Rice 5kg','Grains',60,15,410.00,DATE_ADD(CURDATE(), INTERVAL 180 DAY),3),
('FRU-001','Apples 1kg','Fruits',6,10,155.00,DATE_ADD(CURDATE(), INTERVAL 12 DAY),1);

INSERT INTO orders (order_code,supplier_id,status,total_amount,notes) VALUES
('PO-2026-001',1,'IN_TRANSIT',812.50,'Weekly fresh produce restock'),
('PO-2026-002',2,'PENDING',1360.00,'Dairy replenishment');
