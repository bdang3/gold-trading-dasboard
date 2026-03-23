CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
  id UUID PRIMARY KEY,
  full_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  phone VARCHAR(50) NOT NULL,
  address VARCHAR(500),
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(30) NOT NULL,
  status VARCHAR(30) NOT NULL,
  preferred_language VARCHAR(10),
  email_verified_at TIMESTAMPTZ,
  failed_login_count INT NOT NULL DEFAULT 0,
  locked_until TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ
);

CREATE TABLE refresh_tokens (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id),
  token_hash VARCHAR(255) NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  revoked_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE plans (
  id UUID PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  type VARCHAR(30) NOT NULL,
  name VARCHAR(255) NOT NULL,
  billing_cycle VARCHAR(30) NOT NULL,
  price NUMERIC(12,2) NOT NULL,
  profit_share_percent INT NOT NULL,
  status VARCHAR(30) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE user_plan_history (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id),
  plan_id UUID NOT NULL REFERENCES plans(id),
  started_at TIMESTAMPTZ NOT NULL,
  ended_at TIMESTAMPTZ,
  is_current BOOLEAN NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE strategies (
  id UUID PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name_vi VARCHAR(255) NOT NULL,
  name_en VARCHAR(255) NOT NULL,
  description TEXT,
  monthly_price NUMERIC(12,2) NOT NULL,
  risk_level VARCHAR(50) NOT NULL,
  supported_timeframes TEXT NOT NULL,
  active BOOLEAN NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE risk_rules (
  id UUID PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  params_json TEXT NOT NULL,
  active BOOLEAN NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE port_master (
  id UUID PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  ip_address VARCHAR(100) NOT NULL,
  port_number INT NOT NULL,
  environment VARCHAR(50) NOT NULL,
  broker_binding VARCHAR(100),
  status VARCHAR(30) NOT NULL,
  current_mt5_account_id UUID,
  note TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE mt5_accounts (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id),
  account_number VARCHAR(50) NOT NULL,
  encrypted_password TEXT NOT NULL,
  broker VARCHAR(100) NOT NULL,
  server VARCHAR(200) NOT NULL,
  account_type VARCHAR(30) NOT NULL,
  verification_status VARCHAR(30) NOT NULL,
  verification_message TEXT,
  strategy_id UUID REFERENCES strategies(id),
  timeframe VARCHAR(20),
  risk_rule_id UUID REFERENCES risk_rules(id),
  status VARCHAR(30) NOT NULL,
  admin_action VARCHAR(40) NOT NULL,
  assigned_port_id UUID REFERENCES port_master(id),
  submitted_at TIMESTAMPTZ,
  started_at TIMESTAMPTZ,
  stopped_at TIMESTAMPTZ,
  last_config_updated_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ
);

ALTER TABLE port_master ADD CONSTRAINT fk_port_current_account FOREIGN KEY (current_mt5_account_id) REFERENCES mt5_accounts(id);

CREATE TABLE notifications (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id),
  type VARCHAR(30) NOT NULL,
  title VARCHAR(255) NOT NULL,
  message TEXT NOT NULL,
  read_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE trades (
  id UUID PRIMARY KEY,
  mt5_account_id UUID NOT NULL REFERENCES mt5_accounts(id),
  symbol VARCHAR(50) NOT NULL,
  ticket VARCHAR(100) NOT NULL,
  volume NUMERIC(12,4) NOT NULL,
  entry_price NUMERIC(18,6) NOT NULL,
  stop_loss NUMERIC(18,6),
  take_profit NUMERIC(18,6),
  close_price NUMERIC(18,6),
  result_amount NUMERIC(18,2),
  change_percent NUMERIC(10,4),
  opened_at TIMESTAMPTZ NOT NULL,
  closed_at TIMESTAMPTZ,
  raw_payload_json TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_logs (
  id UUID PRIMARY KEY,
  actor_type VARCHAR(50) NOT NULL,
  actor_id VARCHAR(100),
  actor_name VARCHAR(255),
  action VARCHAR(100) NOT NULL,
  entity_type VARCHAR(100) NOT NULL,
  entity_id VARCHAR(100),
  result VARCHAR(30) NOT NULL,
  message TEXT NOT NULL,
  metadata_json TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE process_logs (
  id UUID PRIMARY KEY,
  mt5_account_id UUID NOT NULL REFERENCES mt5_accounts(id),
  port_id UUID REFERENCES port_master(id),
  action_type VARCHAR(50) NOT NULL,
  result VARCHAR(30) NOT NULL,
  exit_code INT,
  message TEXT NOT NULL,
  config_snapshot_json TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE bot_operations (
  id UUID PRIMARY KEY,
  mt5_account_id UUID NOT NULL REFERENCES mt5_accounts(id),
  type VARCHAR(50) NOT NULL,
  status VARCHAR(30) NOT NULL,
  requested_by_type VARCHAR(50) NOT NULL,
  requested_by_id VARCHAR(100) NOT NULL,
  port_id UUID REFERENCES port_master(id),
  payload_json TEXT,
  result_json TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  completed_at TIMESTAMPTZ
);

CREATE INDEX idx_mt5_user ON mt5_accounts(user_id);
CREATE INDEX idx_mt5_status ON mt5_accounts(status);
CREATE INDEX idx_mt5_verification ON mt5_accounts(verification_status);
CREATE INDEX idx_trades_account_opened ON trades(mt5_account_id, opened_at DESC);
CREATE INDEX idx_notifications_user_created ON notifications(user_id, created_at DESC);

