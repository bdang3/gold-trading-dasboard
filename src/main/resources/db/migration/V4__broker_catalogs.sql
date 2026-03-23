CREATE TABLE brokers (
  id UUID PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE broker_servers (
  id UUID PRIMARY KEY,
  broker_id UUID NOT NULL REFERENCES brokers(id),
  code VARCHAR(100) NOT NULL,
  name VARCHAR(255) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_broker_servers_broker_code UNIQUE (broker_id, code)
);

CREATE INDEX idx_broker_servers_broker_id ON broker_servers(broker_id);
CREATE INDEX idx_broker_servers_active ON broker_servers(active);

WITH seeded_brokers AS (
  INSERT INTO brokers (id, code, name, active, created_at, updated_at)
  VALUES
    (gen_random_uuid(), 'EXNESS', 'Exness', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'IC_MARKETS', 'IC Markets', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'XM', 'XM', TRUE, NOW(), NOW())
  RETURNING id, code
)
INSERT INTO broker_servers (id, broker_id, code, name, active, created_at, updated_at)
SELECT gen_random_uuid(), b.id, s.code, s.name, TRUE, NOW(), NOW()
FROM seeded_brokers b
JOIN (
  VALUES
    ('EXNESS', 'EXNESS_MT5TRIAL14', 'Exness-MT5Trial14'),
    ('EXNESS', 'EXNESS_MT5REAL7', 'Exness-MT5Real7'),
    ('IC_MARKETS', 'ICM_SC_DEMO', 'ICMarketsSC-Demo'),
    ('IC_MARKETS', 'ICM_SC_LIVE', 'ICMarketsSC-Live'),
    ('XM', 'XM_GLOBAL_MT5_7', 'XMGlobal-MT5-7'),
    ('XM', 'XM_GLOBAL_MT5_10', 'XMGlobal-MT5-10')
) AS s(broker_code, code, name)
ON b.code = s.broker_code;
