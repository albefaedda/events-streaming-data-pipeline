insert into ecommerce.customers (id, customer_name, card_number, customer_email, customer_address, `level`) values
('linetbrown67', 'Linet Brown', '3455 5606 6764 114', 'lpedroni0@whitehouse.gov', '56 Di Loreto Terrace', 'platinum'),
('poliver85', 'Peter Oliver', '4066 6385 0323 6028', 'poliver@google.com', '593 Thompson Drive', 'gold'),
('gregv', 'Greg Vane', '5366 2000 0085 6156', 'gvane@skyrock.com', '1 Gale Court', 'silver'),
('jejiri', 'Jeremy Jiri', '3455 5697 7661 474', 'jjiri@discovery.com', '2 Declaration Pass', 'bronze'),
('jubrad', 'Julie Bradnum', '5361 2000 0061 9930', 'jbradnum@so-net.net', '32 Bluestem Avenue', 'gold'),
('gaunight', 'Gaurav Night', '4567 8570 084 31', 'gnight@google.com', '105 Duncan Piece', 'silver'),
('amalee', 'Amanda Leeworth', '3455 5624 6208 172', 'amleeworth@microsoft.com', '10 Stonewell Grove', 'gold'),
('the_l_champion', 'Lisa Champion', '4567 8574 6705 5042', 'lchampion@jetblue.net', '71 Felmersham Court', 'silver'),
('bobby_w', 'Bob West', '3455 5639 9209 084', 'bob.west@gmail.com', '68 Skylark Ridge', 'bronze');


insert into ecommerce.products (id, product_name, product_cost) values
('4G7D2', 'Apple iPhone 15', 949.99),
('R9J5Z', 'Microsoft Surface', 727.94),
('S7E9A', 'Fujifilm XT-5', 1199.99),
('M8N6Y', 'Google Pixel', 1033.85),
('X2W4K', 'One Plus 9', 533.92),
('B3H8R', 'Amazon Alexa Dot', 49.95),
('T5P0L', 'Canon EOS 5', 1027.94),
('Q1F3V', 'Samsung S21', 818.01),
('J6K1U', 'Google Home', 35.95),
('F2C5M', 'One Plus 9', 533.92);


SELECT id, customer_name, customer_email, customer_address, card_number, `level` FROM ecommerce.customers;

SELECT id, product_name, product_cost FROM ecommerce.products;