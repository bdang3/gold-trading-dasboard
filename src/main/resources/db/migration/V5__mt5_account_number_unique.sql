WITH ranked_accounts AS (
  SELECT id,
         account_number,
         ROW_NUMBER() OVER (PARTITION BY account_number ORDER BY created_at NULLS LAST, id) AS row_num
  FROM mt5_accounts
  WHERE account_number IS NOT NULL
)
UPDATE mt5_accounts a
SET account_number = a.account_number || '-' || SUBSTRING(a.id::text, 1, 8)
FROM ranked_accounts r
WHERE a.id = r.id
  AND r.row_num > 1;

ALTER TABLE mt5_accounts
  ADD CONSTRAINT uq_mt5_accounts_account_number UNIQUE (account_number);
