-- Seed users
INSERT INTO users (id, full_name, email, phone, address, password_hash, role, status, preferred_language, failed_login_count, created_at, updated_at)
VALUES
(gen_random_uuid(), 'Admin', 'admin@goldbot.local', '0900000000', 'HCM', '$2a$10$qfwuvQTVNsjSlyj1flBz5ONh9S7M2L4nCCjfd5W2kyfkswQ6hQ5x6', 'ADMIN', 'ACTIVE', 'vi', 0, NOW(), NOW()),
(gen_random_uuid(), 'Nguyễn Văn A', 'nguyen.a@email.com', '0912345678', 'Quận 1, TP.HCM', '$2a$10$qfwuvQTVNsjSlyj1flBz5ONh9S7M2L4nCCjfd5W2kyfkswQ6hQ5x6', 'USER', 'ACTIVE', 'vi', 0, NOW(), NOW()),
(gen_random_uuid(), 'Trần Minh Khang', 'khang.tran@email.com', '0987654321', 'Quận 7, TP.HCM', '$2a$10$qfwuvQTVNsjSlyj1flBz5ONh9S7M2L4nCCjfd5W2kyfkswQ6hQ5x6', 'USER', 'ACTIVE', 'vi', 0, NOW(), NOW()),
(gen_random_uuid(), 'Lê Hoàng Anh', 'anh.le@email.com', '0903112233', 'Cầu Giấy, Hà Nội', '$2a$10$qfwuvQTVNsjSlyj1flBz5ONh9S7M2L4nCCjfd5W2kyfkswQ6hQ5x6', 'USER', 'ACTIVE', 'vi', 0, NOW(), NOW());

INSERT INTO plans (id, code, type, name, billing_cycle, price, profit_share_percent, status, created_at, updated_at) VALUES
(gen_random_uuid(), 'PS20', 'PROFIT_SHARING', 'Profit Sharing 20%', 'MONTHLY', 0, 20, 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'PS30', 'PROFIT_SHARING', 'Profit Sharing 30%', 'MONTHLY', 0, 30, 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'SUB_M', 'SUBSCRIPTION', 'Subscription Monthly', 'MONTHLY', 49, 0, 'COMING_SOON', NOW(), NOW());

INSERT INTO strategies (id, code, name_vi, name_en, description, monthly_price, risk_level, supported_timeframes, active, created_at, updated_at) VALUES
(gen_random_uuid(), 'EMASCALP', 'EMA Scalp', 'EMA Scalp', 'Scalping dựa trên EMA ngắn hạn', 0, 'medium', 'M1,M5', true, NOW(), NOW()),
(gen_random_uuid(), 'EMA_BREAKOUT', 'EMA Breakout', 'EMA Breakout', 'Breakout khi giá vượt EMA', 0, 'high', 'M5,H1', true, NOW(), NOW()),
(gen_random_uuid(), 'TREND_FOLLOW', 'Trend Follow', 'Trend Follow', 'Theo xu hướng dài hạn', 0, 'low', 'H1,D1', true, NOW(), NOW()),
(gen_random_uuid(), 'GRID_SAFE', 'Grid Safe', 'Grid Safe', 'Grid trading an toàn', 0, 'low', 'D1', true, NOW(), NOW());

INSERT INTO risk_rules (id, code, name, description, params_json, active, created_at, updated_at) VALUES
(gen_random_uuid(), 'CONSERVATIVE', 'Conservative', 'Lot size nhỏ, SL chặt, max 2% per trade', '{"maxRiskPercent":2,"maxLotSize":0.5,"stopLossMultiplier":1.0}', true, NOW(), NOW()),
(gen_random_uuid(), 'MODERATE', 'Moderate', 'Lot size trung bình, SL vừa, max 5% per trade', '{"maxRiskPercent":5,"maxLotSize":1.0,"stopLossMultiplier":1.5}', true, NOW(), NOW()),
(gen_random_uuid(), 'AGGRESSIVE', 'Aggressive', 'Lot size lớn, SL rộng, max 10% per trade', '{"maxRiskPercent":10,"maxLotSize":3.0,"stopLossMultiplier":2.0}', true, NOW(), NOW());

INSERT INTO port_master (id, code, ip_address, port_number, environment, broker_binding, status, note, created_at, updated_at) VALUES
(gen_random_uuid(), 'P001', '103.28.41.12', 8501, 'production', 'Exness', 'OCCUPIED', '', NOW(), NOW()),
(gen_random_uuid(), 'P002', '103.28.41.12', 8502, 'production', 'IC Markets', 'OCCUPIED', '', NOW(), NOW()),
(gen_random_uuid(), 'P003', '103.28.41.13', 8501, 'production', 'Exness', 'OCCUPIED', '', NOW(), NOW()),
(gen_random_uuid(), 'P004', '103.28.41.13', 8502, 'production', 'IC Markets', 'AVAILABLE', '', NOW(), NOW()),
(gen_random_uuid(), 'P005', '103.28.41.14', 8501, 'staging', 'IC Markets', 'OCCUPIED', 'Staging test', NOW(), NOW()),
(gen_random_uuid(), 'P006', '103.28.41.14', 8502, 'staging', 'XM', 'AVAILABLE', '', NOW(), NOW()),
(gen_random_uuid(), 'P007', '103.28.41.15', 8501, 'production', 'Exness', 'DISABLED', 'Maintenance', NOW(), NOW()),
(gen_random_uuid(), 'P008', '103.28.41.15', 8502, 'production', 'XM', 'AVAILABLE', '', NOW(), NOW());
