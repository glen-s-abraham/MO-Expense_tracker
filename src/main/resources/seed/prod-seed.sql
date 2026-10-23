-- =============================================================================
-- Mushroom Expense Tracker - Production Seed Data for H2
-- Source: 192.168.1.50:5432/mariasorganicsdb
-- Generated: 2026-10-22T21:35:52+05:30
-- =============================================================================

--
-- PostgreSQL database dump
--


-- Dumped from database version 14.24 (Ubuntu 14.24-0ubuntu0.22.04.1)
-- Dumped by pg_dump version 14.24 (Ubuntu 14.24-0ubuntu0.22.04.1)


--
-- Data for Name: categories; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO categories (id, name, type) VALUES (1, 'Raw Materials', 'EXPENSE');
INSERT INTO categories (id, name, type) VALUES (2, 'Production Consumables', 'EXPENSE');
INSERT INTO categories (id, name, type) VALUES (3, 'Packaging & Sales', 'EXPENSE');
INSERT INTO categories (id, name, type) VALUES (4, 'Utilities', 'EXPENSE');
INSERT INTO categories (id, name, type) VALUES (5, 'Repairs & Maintenance', 'EXPENSE');
INSERT INTO categories (id, name, type) VALUES (6, 'Labor & Staff', 'EXPENSE');
INSERT INTO categories (id, name, type) VALUES (7, 'Admin & Office', 'EXPENSE');
INSERT INTO categories (id, name, type) VALUES (8, 'Marketing', 'EXPENSE');
INSERT INTO categories (id, name, type) VALUES (9, 'Capital Assets', 'EXPENSE');
INSERT INTO categories (id, name, type) VALUES (10, 'Financial', 'EXPENSE');
INSERT INTO categories (id, name, type) VALUES (11, 'Income From Sales', 'INCOME');
INSERT INTO categories (id, name, type) VALUES (12, 'Capital Introduced', 'INCOME');
INSERT INTO categories (id, name, type) VALUES (13, 'Loan', 'INCOME');
INSERT INTO categories (id, name, type) VALUES (14, 'Other Income', 'INCOME');


--
-- Data for Name: sub_categories; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO sub_categories (id, name, category_id) VALUES (1, 'Spawn / Spores', 1);
INSERT INTO sub_categories (id, name, category_id) VALUES (2, 'Pellets', 1);
INSERT INTO sub_categories (id, name, category_id) VALUES (3, 'Substrate', 1);
INSERT INTO sub_categories (id, name, category_id) VALUES (4, 'PP Bags / Sleeves', 1);
INSERT INTO sub_categories (id, name, category_id) VALUES (5, 'Microporous Tape', 1);
INSERT INTO sub_categories (id, name, category_id) VALUES (6, 'Rubbing Alcohol / Sanitizers', 2);
INSERT INTO sub_categories (id, name, category_id) VALUES (8, 'Face Masks & Gloves', 2);
INSERT INTO sub_categories (id, name, category_id) VALUES (9, 'Cleaning Chemicals', 2);
INSERT INTO sub_categories (id, name, category_id) VALUES (10, 'Filter Pads / Cotton', 2);
INSERT INTO sub_categories (id, name, category_id) VALUES (11, 'Plastic Punnets / Trays', 3);
INSERT INTO sub_categories (id, name, category_id) VALUES (12, 'Cling Film / Shrink Wrap', 3);
INSERT INTO sub_categories (id, name, category_id) VALUES (13, 'Sticker Labels', 3);
INSERT INTO sub_categories (id, name, category_id) VALUES (14, 'Cardboard Cartons (Bulk)', 3);
INSERT INTO sub_categories (id, name, category_id) VALUES (15, 'Delivery Fuel / Logistics Charges', 3);
INSERT INTO sub_categories (id, name, category_id) VALUES (16, 'Electricity - Grow Rooms (AC/Humidifier)', 4);
INSERT INTO sub_categories (id, name, category_id) VALUES (17, 'Electricity - General', 4);
INSERT INTO sub_categories (id, name, category_id) VALUES (18, 'Water Charges', 4);
INSERT INTO sub_categories (id, name, category_id) VALUES (19, 'Generator Fuel (Diesel/Petrol)', 4);
INSERT INTO sub_categories (id, name, category_id) VALUES (20, 'Gas / Firewood (for Sterilization)', 4);
INSERT INTO sub_categories (id, name, category_id) VALUES (21, 'Machinery Repair (AC, Fridge, Humidifier)', 5);
INSERT INTO sub_categories (id, name, category_id) VALUES (22, 'Building/Shed Maintenance', 5);
INSERT INTO sub_categories (id, name, category_id) VALUES (23, 'Electrical Maintenance', 5);
INSERT INTO sub_categories (id, name, category_id) VALUES (24, 'Vehicle Maintenance', 5);
INSERT INTO sub_categories (id, name, category_id) VALUES (25, 'Casual Labor (Daily Wagers)', 6);
INSERT INTO sub_categories (id, name, category_id) VALUES (26, 'Permanent Staff Salary', 6);
INSERT INTO sub_categories (id, name, category_id) VALUES (27, 'Staff Food / Tea Expenses', 6);
INSERT INTO sub_categories (id, name, category_id) VALUES (28, 'Overtime', 6);
INSERT INTO sub_categories (id, name, category_id) VALUES (29, 'Rent / Lease Payments', 7);
INSERT INTO sub_categories (id, name, category_id) VALUES (30, 'Printing & Stationery', 7);
INSERT INTO sub_categories (id, name, category_id) VALUES (31, 'Phone / Internet / Software Subs', 7);
INSERT INTO sub_categories (id, name, category_id) VALUES (32, 'Professional Fees (Vet, Accountant, Consultant)', 7);
INSERT INTO sub_categories (id, name, category_id) VALUES (33, 'Licenses & Permits', 7);
INSERT INTO sub_categories (id, name, category_id) VALUES (34, 'Digital Ads (FB/Insta/Google)', 8);
INSERT INTO sub_categories (id, name, category_id) VALUES (35, 'Print Ads / Flyers', 8);
INSERT INTO sub_categories (id, name, category_id) VALUES (36, 'Free Samples / Promotional Giveaways', 8);
INSERT INTO sub_categories (id, name, category_id) VALUES (37, 'New Machinery (Purchase)', 9);
INSERT INTO sub_categories (id, name, category_id) VALUES (38, 'Infrastructure (Racks, Shed Construction)', 9);
INSERT INTO sub_categories (id, name, category_id) VALUES (39, 'Vehicles', 9);
INSERT INTO sub_categories (id, name, category_id) VALUES (40, 'Computer / Electronic Equipment', 9);
INSERT INTO sub_categories (id, name, category_id) VALUES (41, 'Bank Charges', 10);
INSERT INTO sub_categories (id, name, category_id) VALUES (42, 'Interest Payments', 10);
INSERT INTO sub_categories (id, name, category_id) VALUES (43, 'Tax Payments (GST/TDS)', 10);
INSERT INTO sub_categories (id, name, category_id) VALUES (44, 'Grow Kit', 1);


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO users (id, enabled, password, role, username) VALUES (4, true, '$2a$10$YHZSja4wjLK0aQstBBrPvuxcXj/Gm03EUoBhiKCs7sDbd4MUJgxca', 'ROLE_ADMIN', 'admin');
INSERT INTO users (id, enabled, password, role, username) VALUES (6, true, '$2a$10$k/kEAHy5yuSWPFHh/3BfO.D7H0Y5QMtz2r7GIQUOoYWSDGglOuWGy', 'ROLE_MANAGER', 'liya');
INSERT INTO users (id, enabled, password, role, username) VALUES (7, true, '$2a$10$nJQdAdQTO2/FCXQEkos4KeJl1Nf3nqqdFaCgdaP0JNo1cnIs1vKWu', 'ROLE_ACCOUNTANT', 'madhuri');
INSERT INTO users (id, enabled, password, role, username) VALUES (8, true, '$2a$10$HEbWDhePv.tvlRXs0uM8JeqFp/EttZyJXQA71dkrFyXRsEvZCrzE2', 'ROLE_SUPERVISOR', 'glen-sup');
INSERT INTO users (id, enabled, password, role, username) VALUES (5, true, '$2a$10$9LvThJbnwKmA/mUUy1x5WuEYbq1or3awpSdzDlKcfd/J8QGlwwHMa', 'ROLE_ACCOUNTANT', 'glen-man');


--
-- Data for Name: expenses; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (20, 1704, '', '2026-07-10', 'Air blower + 5 rupees platform fee', 'UPI', NULL, 'APPROVED', 0, 9, 40, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (6, 170000, '', '2026-01-01', 'Shed Construction ', 'UPI', NULL, 'APPROVED', 0, 9, 38, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (1, 4980, '', '2025-12-17', 'Spawn 52 Nos. Pellets 100 Kg', 'UPI', NULL, 'APPROVED', 0, 1, 44, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (5, 7112, '', '2025-12-31', 'Label printer Kit - Rs.5 Additional for amazon platform fee', 'CARD', NULL, 'APPROVED', 0, 9, 40, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (12, 11000, '', '2026-07-10', 'Racks x 5 - Rs.5 platform fee', 'CARD', NULL, 'APPROVED', 0, 9, 38, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (26, 400, '', '2026-07-10', 'Nylon Rope 2.5kg', 'UPI', NULL, 'APPROVED', 0, 9, 38, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (11, 1004, '', '2026-01-05', 'Mop - 999 + 5 rupees platform fee', 'UPI', NULL, 'APPROVED', 0, 2, 10, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (8, 325, '', '2026-01-03', 'Duct tape', 'UPI', NULL, 'APPROVED', 0, 5, 22, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (9, 7754.5, '', '2026-01-03', 'PC', 'CARD', NULL, 'APPROVED', 0, 9, 40, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (10, 899.5, '', '2026-01-03', 'Keyboard and mouse', 'CARD', NULL, 'APPROVED', 0, 9, 40, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (7, 885, '', '2026-01-02', 'Tally Software', 'UPI', NULL, 'APPROVED', 0, 7, 31, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (13, 4980, '', '2026-01-14', 'Spawn 52 Nos. Pellets 100 Kg', 'CASH', NULL, 'APPROVED', 0, 1, 44, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (17, 519, '', '2026-01-31', 'Stock Loge book + Day Book', 'CARD', NULL, 'APPROVED', 0, 7, 30, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (15, 1527, '', '2026-01-24', '1500 stickers cherupushpam', 'UPI', NULL, 'APPROVED', 0, 3, 13, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (2, 136000, '', '2025-12-09', 'Mushroom Shed Advance to Back2Farm', 'UPI', NULL, 'REJECTED', 0, 9, 38, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (4, 34000, '', '2025-12-23', 'Shed remaining balance', 'UPI', NULL, 'REJECTED', 0, 9, 38, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (36, 1260, '', '2026-03-24', 'pp bag + neckring', 'UPI', NULL, 'APPROVED', 0, 1, 4, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (29, 1785, '', '2026-04-05', 'Spawn 25, pellettes25 kg', 'CASH', NULL, 'APPROVED', 0, 1, 44, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (32, 500, '', '2026-03-01', '2 kg', 'CASH', NULL, 'APPROVED', 0, 1, 4, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (16, 752.95, '', '2026-07-10', 'Waste Bin From Amazon', 'CARD', NULL, 'APPROVED', 0, 9, 38, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (28, 250, '', '2026-02-21', '1kg 12x18 clear cover', 'CASH', NULL, 'APPROVED', 0, 1, 4, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (18, 11490, '', '2026-02-03', 'Fridge', 'CARD', NULL, 'APPROVED', 0, 9, 40, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (14, 1715, '', '2026-07-10', 'Plastic crates - For mushroom bag transportation', 'CARD', NULL, 'APPROVED', 0, 9, 38, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (23, 211, '', '2026-02-09', 'cling film', 'UPI', NULL, 'APPROVED', 0, 3, 12, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (63, 460, '', '2026-06-23', 'Pp bag 16*12', 'UPI', NULL, 'APPROVED', 0, 1, 4, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (19, 644, '', '2026-07-10', '16 amps extension cord', 'UPI', NULL, 'APPROVED', 0, 9, 40, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (24, 242, '', '2026-02-09', 'tape 12 pax', 'UPI', NULL, 'APPROVED', 0, 1, 5, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (22, 854, '', '2026-02-07', 'Netted Storage box for refridgeration', 'UPI', NULL, 'APPROVED', 0, 3, 11, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (27, 4980, '', '2026-02-21', 'Leenas Mushroom 100 bag grow kit', 'UPI', NULL, 'APPROVED', 0, 1, 44, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (33, 4980, '', '2026-02-14', 'grow kit x 100', 'UPI', NULL, 'APPROVED', 0, 1, 44, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (35, 300, '', '2026-04-07', '1kg -12x18', 'CASH', NULL, 'APPROVED', 0, 1, 4, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (25, 837, '', '2026-02-21', '50 pcs 1000cc', 'UPI', NULL, 'APPROVED', 0, 3, 11, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (30, 6000, '', '2026-04-05', 'Rack', 'UPI', NULL, 'APPROVED', 0, 9, 38, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (31, 1000, '', '2026-03-01', '100 box', 'CASH', NULL, 'APPROVED', 0, 3, 11, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (34, 500, '', '2026-04-07', 'boxes x 50 ', 'CASH', NULL, 'APPROVED', 0, 3, 11, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (38, 350, '', '2026-04-07', 'Courrier', 'CASH', NULL, 'APPROVED', 0, 3, 15, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (37, 350, '', '2026-04-07', '7*15 pp bag', 'UPI', NULL, 'APPROVED', 0, 1, 4, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (39, 350, '', '2026-04-13', '8x16 - 500g pp', 'UPI', NULL, 'APPROVED', 0, 1, 4, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (40, 5000, '', '2026-04-16', '100 kg pellet + 52 bag spawn', 'UPI', NULL, 'APPROVED', 0, 1, 44, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (41, 2457, '', '2026-04-23', 'rack 6 shelf', 'UPI', NULL, 'APPROVED', 0, 9, 38, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (42, 2355, '', '2026-05-01', '50kg kit', 'UPI', NULL, 'APPROVED', 0, 1, 44, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (45, 199.98, '', '2026-05-09', '100m', 'UPI', NULL, 'APPROVED', 0, 3, 12, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (43, 250, '', '2026-05-07', '12 pack', 'UPI', NULL, 'APPROVED', 0, 1, 5, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (46, 489, '', '2026-05-11', '1 kg 8*16', 'CARD', NULL, 'APPROVED', 0, 1, 4, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (62, 6125, '', '2026-06-12', '125 qty', 'UPI', NULL, 'APPROVED', 0, 1, 44, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (59, 603, '', '2026-06-10', '12 inch tie 300', 'UPI', NULL, 'APPROVED', 0, 1, 4, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (60, 750, '', '2026-06-10', 'LMOM', 'UPI', NULL, 'APPROVED', 0, 7, 33, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (58, 599, '', '2026-06-07', 'storage box  6 nos', 'UPI', NULL, 'APPROVED', 0, 3, 11, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (57, 500, '', '2026-06-06', 'FSSAI 5 Year registration', 'UPI', NULL, 'APPROVED', 0, 7, 33, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (56, 250, '', '2026-06-06', 'Punnet box shipping', 'CASH', NULL, 'APPROVED', 0, 3, 15, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (55, 1104, '', '2026-06-02', 'cash box', 'CARD', NULL, 'APPROVED', 0, 7, 30, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (54, 1348, '', '2026-06-02', '200 - 700/1000 1000 ml punnet ', 'UPI', NULL, 'APPROVED', 0, 3, 11, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (53, 497, '', '2026-05-28', '700/1000 ml box - 50', 'UPI', NULL, 'APPROVED', 0, 3, 11, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (52, 422, '', '2026-05-25', '50xpunnet box', 'UPI', NULL, 'APPROVED', 0, 3, 11, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (50, 380, '', '2026-05-23', 'carrubag and ppcover', 'CASH', NULL, 'APPROVED', 0, 1, 4, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (49, 276.3, '', '2026-05-18', 'Rat traps ', 'CARD', NULL, 'APPROVED', 0, 5, 22, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (48, 6500, '', '2026-05-13', '125kg', 'UPI', NULL, 'APPROVED', 0, 1, 44, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (47, 382, '', '2026-05-11', 'Cable tie 100', 'CARD', NULL, 'APPROVED', 0, 1, 4, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (21, 1709, '', '2026-07-10', 'Stabilizer', 'CARD', NULL, 'APPROVED', 0, 9, 40, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (64, 450, '', '2026-06-28', '1.5 litre mugh', 'UPI', NULL, 'APPROVED', 0, 9, 37, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (44, 500, '', '2026-05-09', '50', 'CASH', NULL, 'APPROVED', 0, 3, 11, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (109, 556, '', '2026-10-14', 'Oil Extractor 200cc', 'CASH', NULL, 'APPROVED', 0, 9, 37, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (83, 594, '', '2026-08-12', 'Printing label 75x75 DT', 'UPI', NULL, 'APPROVED', 0, 7, 30, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (67, 4000, '', '2026-07-10', 'Taxation Saju & Co', 'UPI', NULL, 'APPROVED', 0, 7, 32, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (65, 253, '', '2026-06-28', 'Shade net', 'UPI', NULL, 'APPROVED', 0, 5, 22, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (66, 399, '', '2026-06-28', '100m x 2', 'UPI', NULL, 'APPROVED', 0, 3, 12, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (51, 400, '', '2026-05-23', 'dr. mush contingency stock spoilage', 'CASH', NULL, 'APPROVED', 0, 8, 36, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (68, 5000, '', '2026-07-13', '100kg grow kit', 'UPI', NULL, 'APPROVED', 0, 1, 44, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (69, 240, '', '2026-07-17', 'additional', 'UPI', NULL, 'APPROVED', 0, 1, 1, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (70, 1350, '', '2026-07-17', '700/1000 1000ml containers x 200 ', 'UPI', NULL, 'APPROVED', 0, 3, 11, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (94, 3750, '', '2026-09-14', '75 bags', 'UPI', NULL, 'APPROVED', 0, 1, 1, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (71, 3019.04, '', '2026-07-18', 'Tapo Sensor Hub + Tapo Temperature & Humidity Sensor', 'UPI', NULL, 'APPROVED', 0, 9, 40, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (72, 505, '', '2026-07-20', 'Fly Trap + Hooks', 'UPI', NULL, 'APPROVED', 0, 2, 9, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (85, 694, '', '2026-08-13', 'catridge', 'UPI', NULL, 'APPROVED', 0, 7, 30, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (73, 304, '', '2026-07-20', 'food grade h2o2', 'UPI', NULL, 'APPROVED', 0, 2, 6, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (84, 5500, '', '2026-08-13', '100 nos', 'UPI', NULL, 'REJECTED', 0, 1, 44, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (74, 620, '', '2026-07-21', 'Pp bag', 'CASH', NULL, 'APPROVED', 0, 1, 4, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (97, 5250, '', '2026-09-15', '150kg ', 'UPI', NULL, 'APPROVED', 0, 1, 2, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (76, 300, '', '2026-07-22', '200 box delivery', 'CASH', NULL, 'APPROVED', 0, 3, 15, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (86, 4375, '', '2026-08-17', '125 kg pelletes', 'UPI', NULL, 'APPROVED', 0, 1, 2, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (77, 573, '', '2026-07-23', 'Veg sticker + sealing sticker', 'UPI', NULL, 'APPROVED', 0, 3, 13, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (78, 1280, '', '2026-07-23', 'Label Rolls - thermal printer', 'UPI', NULL, 'APPROVED', 0, 3, 13, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (79, 403, '', '2026-07-25', '2 cling film', 'UPI', NULL, 'APPROVED', 0, 3, 12, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (87, 1350, '', '2026-08-19', '200 - 700/1000 - 1000ml', 'UPI', NULL, 'APPROVED', 0, 3, 11, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (80, 265, '', '2026-07-28', '12 pack', 'UPI', NULL, 'APPROVED', 0, 1, 5, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (81, 19116, '', '2026-08-07', 'FSSAI Complaince Test - Eco green Lab', 'BANK_TRANSFER', NULL, 'APPROVED', 0, 7, 33, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (103, 999, '', '2026-10-05', '8x16 2 kg', 'CASH', NULL, 'APPROVED', 0, 1, 4, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (82, 1202, '', '2026-08-10', '1kg 8x16 pp bag + 300 ties', 'UPI', NULL, 'APPROVED', 0, 1, 4, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (88, 3150, '', '2026-08-19', '63 -spawn', 'UPI', NULL, 'APPROVED', 0, 1, 1, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (98, 1000, '', '2026-09-17', 'Pad replacement charges', 'UPI', NULL, 'APPROVED', 0, 6, 25, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (89, 1500, '', '2026-08-20', 'aps charges', 'UPI', NULL, 'APPROVED', 0, 3, 15, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (90, 2500, '', '2026-08-29', 'Pad replacement sponge', 'UPI', NULL, 'APPROVED', 0, 5, 22, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (104, 398, '', '2026-10-05', 'cling', 'CASH', NULL, 'APPROVED', 0, 3, 12, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (91, 4600, '', '2026-08-29', 'Pp ring and cap ', 'UPI', NULL, 'APPROVED', 0, 1, 4, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (99, 10000, '', '2026-09-17', 'Pad replacement', 'UPI', NULL, 'APPROVED', 0, 5, 22, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (92, 620, '', '2026-09-05', 'Pp bags', 'CASH', NULL, 'APPROVED', 0, 1, 4, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (105, 299, '', '2026-10-05', 'Funnels', 'CASH', NULL, 'APPROVED', 0, 9, 37, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (96, 199, '', '2026-09-15', 'cling', 'UPI', NULL, 'APPROVED', 0, 3, 12, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (100, 1289, '', '2026-09-24', 'Plugs + 6 amps Wifi socket', 'UPI', NULL, 'APPROVED', 0, 9, 40, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (110, 247, '', '2026-10-15', 'Seaweed - tonic', 'CASH', NULL, 'APPROVED', 0, 2, 6, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (95, 999, '', '2026-09-15', 'Steel bucket', 'UPI', NULL, 'REJECTED', 0, 9, 37, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (106, 304, '', '2026-10-05', 'Syringle Large', 'CASH', NULL, 'APPROVED', 0, 9, 37, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (101, 223, '', '2026-09-25', 'cotton', 'UPI', NULL, 'APPROVED', 0, 2, 10, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (102, 998, '', '2026-09-26', 'shed maintetance', 'UPI', NULL, 'APPROVED', 0, 5, 22, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (107, 544, '', '2026-10-07', 'Plastic sheet', 'CASH', NULL, 'APPROVED', 0, 5, 22, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (108, 1528, '', '2026-10-08', 'hinged box 1000ml', 'CASH', NULL, 'APPROVED', 0, 3, 11, 5);
INSERT INTO expenses (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, sub_category_id, user_id) VALUES (111, 8000, '', '2026-10-15', '150 nos', 'CASH', NULL, 'APPROVED', 0, 1, 44, 5);


--
-- Data for Name: expense_attachments; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (6, '83a130ce-9059-4d38-86b7-041249c7b578_IMG-20251223-WA0001.jpg', 4);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (7, '811761fc-9ddc-42af-a389-89f6a25dea90_Order Details 2-1.png', 5);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (8, 'fc6b5221-e8d5-4bc7-9365-ce810d6b532a_Order Details 3-1.png', 5);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (9, 'c5b77686-4252-473b-aa3d-b3759b2b1b21_Order Details-1.png', 5);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (10, 'bb44baea-02a5-4a57-9aed-58da65876bce_WhatsApp Image 2025-12-31 at 9.59.51 AM.jpeg', 5);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (11, '23a02180-2b1f-4eb4-a813-2dcd64cb53f1_WhatsApp Image 2025-12-10 at 11.12.02 PM.jpeg', 2);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (12, '497bff27-9ccd-4ec2-8b1b-7c1fbcb24985_WhatsApp Image 2025-12-09 at 11.09.38 PM.jpeg', 2);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (13, '96e6847b-9a45-40cb-b8fa-d77ddfe8ea45_WhatsApp Image 2025-12-09 at 11.09.19 PM.jpeg', 2);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (14, '2824355f-312e-499d-90ce-e70e9189e2f2_WhatsApp Image 2025-12-17 at 4.59.27 PM.jpeg', 1);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (16, '900f7416-940d-4afd-9c82-dcdfd2ee20e5_34k.jpeg', 6);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (17, 'bb3307e7-371d-46eb-946e-54f714a0ca7e_36k.jpeg', 6);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (18, '3561a216-1d44-4f2d-b920-dee9dcedc84e_50k.jpeg', 6);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (19, '3bae941d-02ed-4304-8c5c-6793a23273bf_50k2.jpeg', 6);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (20, '7559f183-39f2-4c58-b9ed-6f941b3616d5_B2C-047525-26 (1)-1.png', 6);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (21, 'bbe03488-2c08-41cc-bd27-0502f5661507_B2C-047525-26 (1)-2.png', 6);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (22, 'aee9daad-51a6-4cbc-8752-22afd2253e96_Capture.PNG', 7);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (23, '5fd5ca0a-3a63-4af0-9dd8-4a4635458025_WhatsApp Image 2026-01-02 at 10.02.14 AM.jpeg', 7);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (24, 'b0fb1fb7-f37f-49a4-8528-ef00e60548e3_IMG-20260103-WA0001.jpg', 8);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (25, 'bb30981a-1fbe-42dd-8349-637bfd95f662_pc-1.png', 9);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (26, 'd932f97b-e3ce-43bd-ac47-2c833995c00a_pc-2.png', 9);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (27, '1417ae01-80de-4a06-bed1-3da6dc42d339_keyboard mouse-2.png', 10);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (28, 'a08d3416-2900-4427-9db0-a3f66b64e1ce_keyboard mouse-1.png', 10);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (29, 'debf7514-e34b-4914-875d-da130e39ab9e_Order Details.pdf', 11);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (30, 'c3544125-b592-493e-a660-1c228f005963_invoice mop.pdf', 11);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (32, '31953df4-9f43-4e88-8d1b-4a7c27ecff3f_invoice.pdf', 12);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (33, 'de27eeee-45f0-4ecb-a519-84bd2db8fc0f_invoice (1).pdf', 12);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (34, 'c4a8dbe9-e288-43e8-a7fa-9eee2f91b905_invoice (2).pdf', 12);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (35, '23ef3bda-63b6-4b0a-b74b-8cfeb563b8c9_invoice (3).pdf', 12);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (36, '7a5e4ec2-4644-47e9-ba12-ed6976c05024_invoice (4).pdf', 12);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (37, 'f6de7c31-16c2-413b-a389-90360a258230_Order Details.pdf', 12);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (38, '1115e3cc-44b8-4b42-93c8-ab4cd96f203e_leenas invoice jan 10.pdf', 1);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (39, '9c67003f-b1b1-40bf-8ba3-cd417ec48944_WhatsApp Image 2026-01-14 at 4.59.08 PM.jpeg', 13);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (40, 'b9b10c30-cabc-45ce-9a2f-8d2687ee6d09_invoice (1).pdf', 14);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (41, 'bdb7d884-6e04-49f0-bdfa-a3bf0c57f34c_IMG_20260124_122757.jpg', 15);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (42, '5f7cb5fc-082e-4662-9400-3f8e306668ff_fridge.pdf', 18);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (43, 'dd358841-9f1b-4c84-a9e8-cf0d065250c2_stabilizer.pdf', 21);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (44, '35f1d533-ba9a-4352-a804-ebd8157aff40_vaccum.pdf', 20);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (45, '2120f225-f249-4c64-9186-6f055bdcd5f0_WhatsApp Image 2026-02-07 at 1.02.19 PM.jpeg', 13);
INSERT INTO expense_attachments (id, file_name, expense_id) VALUES (46, 'd1391924-96fc-4007-9147-8e37dfcaa16f_Eco greenlabs payent transaction id.jpeg', 81);


--
-- Data for Name: expense_comments; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO expense_comments (id, message, timestamp, expense_id, user_id) VALUES (1, 'Wrong payment mode', '2025-12-17 20:46:27.75911', 2, 7);
INSERT INTO expense_comments (id, message, timestamp, expense_id, user_id) VALUES (2, 'Wrong payment mode', '2025-12-17 20:46:44.673956', 1, 7);
INSERT INTO expense_comments (id, message, timestamp, expense_id, user_id) VALUES (3, 'Attach proof', '2025-12-31 10:10:25.639708', 2, 7);
INSERT INTO expense_comments (id, message, timestamp, expense_id, user_id) VALUES (4, 'Receipt not uploaded', '2025-12-31 10:25:48.456676', 1, 7);
INSERT INTO expense_comments (id, message, timestamp, expense_id, user_id) VALUES (5, 'Revert for single', '2025-12-31 10:29:01.788531', 2, 7);
INSERT INTO expense_comments (id, message, timestamp, expense_id, user_id) VALUES (6, 'Revert for single', '2025-12-31 10:29:19.779249', 4, 7);
INSERT INTO expense_comments (id, message, timestamp, expense_id, user_id) VALUES (7, 'Invoice not found', '2026-01-10 14:05:52.50725', 1, 7);
INSERT INTO expense_comments (id, message, timestamp, expense_id, user_id) VALUES (8, 'Not accurate', '2026-08-17 21:26:26.283663', 84, 5);
INSERT INTO expense_comments (id, message, timestamp, expense_id, user_id) VALUES (9, 'Cancelled order', '2026-09-24 09:24:04.264009', 95, 5);


--
-- Data for Name: incomes; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (31, 300, '', '2026-10-17', 'Sridevi veg store', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (2, 40000, '', '2026-02-01', 'By Glen', 'BANK_TRANSFER', NULL, 'APPROVED', 0, 12, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (32, 1575, '', '2026-10-17', 'Gopalan ', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (15, 825, '', '2026-08-19', 'Gopalan', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (16, 310, '', '2026-08-19', 'private', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (7, 55000, '', '2026-07-01', 'By Glen', 'BANK_TRANSFER', NULL, 'APPROVED', 0, 12, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (5, 3375, '', '2026-06-30', 'June Sales', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (4, 1725, '', '2026-05-31', 'may sales', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (3, 2847, '', '2026-04-30', 'april sales', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (17, 150, '', '2026-08-20', 'private', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (1, 170000, '', '2026-02-01', 'Unsecured Loan From Father', 'OTHERS', NULL, 'APPROVED', 0, 13, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (33, 600, '', '2026-10-19', 'Deva', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (8, 50000, '', '2026-04-01', 'By Glen', 'CASH', NULL, 'APPROVED', 0, 12, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (18, 300, '', '2026-08-23', 'Deva communications', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (19, 1200, '', '2026-08-29', 'Gopalan', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (20, 10000, '', '2026-09-01', 'by me', 'UPI', NULL, 'APPROVED', 0, 12, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (21, 750, '', '2026-09-07', 'Civil station', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (9, 10000, '', '2026-08-01', 'by me', 'BANK_TRANSFER', NULL, 'APPROVED', 0, 12, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (6, 8955, '', '2026-07-30', 'July till 30th', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (10, 300, '', '2026-08-03', 'deva', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (11, 1050, '', '2026-08-06', 'Gopalan', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (22, 825, '', '2026-09-08', 'Deva', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (12, 750, '', '2026-08-11', 'Gopalan chetan', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (13, 375, '', '2026-08-12', 'Deva Reji', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (23, 3425, '', '2026-09-23', 'sales', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (24, 150, '', '2026-09-23', 'local', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (14, 15000, '', '2026-08-12', 'by e', 'BANK_TRANSFER', NULL, 'APPROVED', 0, 12, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (25, 600, '', '2026-09-29', 'deva', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (26, 375, '', '2026-09-29', 'deva', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (27, 10000, '', '2026-10-01', 'Capital by me', 'CASH', NULL, 'APPROVED', 0, 12, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (29, 375, '', '2026-10-05', 'deva', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (28, 525, '', '2026-10-04', 'Gopalan', 'CASH', NULL, 'APPROVED', 0, 11, 5);
INSERT INTO incomes (id, amount, batch_id, date, description, payment_mode, receipt_image, status, tax_percentage, category_id, user_id) VALUES (30, 150, '', '2026-10-13', 'private', 'CASH', NULL, 'APPROVED', 0, 11, 5);


--
-- Data for Name: income_attachments; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: income_comments; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO income_comments (id, message, timestamp, income_id, user_id) VALUES (1, 'not capital', '2026-07-10 19:18:46.597319', 1, 5);
INSERT INTO income_comments (id, message, timestamp, income_id, user_id) VALUES (2, 'reduce capital', '2026-08-13 08:53:09.597672', 14, 5);
INSERT INTO income_comments (id, message, timestamp, income_id, user_id) VALUES (3, '-25', '2026-08-19 15:34:32.837279', 15, 5);


--
-- Name: categories_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--



--
-- Name: expense_attachments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--



--
-- Name: expense_comments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--



--
-- Name: expenses_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--



--
-- Name: income_attachments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--



--
-- Name: income_comments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--



--
-- Name: incomes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--



--
-- Name: sub_categories_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--



--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--



--
-- PostgreSQL database dump complete
--



-- =============================================================================
-- Synchronize H2 IDENTITY sequences with maximum imported IDs
-- =============================================================================
ALTER TABLE categories ALTER COLUMN id RESTART WITH 15;
ALTER TABLE sub_categories ALTER COLUMN id RESTART WITH 45;
ALTER TABLE users ALTER COLUMN id RESTART WITH 9;
ALTER TABLE expenses ALTER COLUMN id RESTART WITH 112;
ALTER TABLE expense_attachments ALTER COLUMN id RESTART WITH 47;
ALTER TABLE expense_comments ALTER COLUMN id RESTART WITH 10;
ALTER TABLE incomes ALTER COLUMN id RESTART WITH 34;
ALTER TABLE income_attachments ALTER COLUMN id RESTART WITH 1;
ALTER TABLE income_comments ALTER COLUMN id RESTART WITH 4;
