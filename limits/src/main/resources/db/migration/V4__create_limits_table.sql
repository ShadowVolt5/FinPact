CREATE SCHEMA IF NOT EXISTS limits;

CREATE TABLE IF NOT EXISTS limits.limits_profiles (
      owner_id        BIGINT PRIMARY KEY,

      base_currency   CHAR(3) NOT NULL DEFAULT 'RUB',
      per_txn         NUMERIC(19,4) NOT NULL,
      daily           NUMERIC(19,4) NOT NULL,
      monthly         NUMERIC(19,4) NOT NULL,
      currencies      TEXT[] NOT NULL,

      kyc_verified    BOOLEAN NOT NULL DEFAULT TRUE,
      sanctions_clear BOOLEAN NOT NULL DEFAULT TRUE,

      created_at      TIMESTAMP NOT NULL DEFAULT now(),
      updated_at      TIMESTAMP NOT NULL DEFAULT now(),

      CONSTRAINT limits_profiles_owner_fk
          FOREIGN KEY (owner_id)
              REFERENCES auth.users(id)
              ON UPDATE CASCADE
              ON DELETE CASCADE,

      CONSTRAINT limits_profiles_base_currency_chk CHECK (char_length(base_currency) = 3),
      CONSTRAINT limits_profiles_per_txn_chk CHECK (per_txn >= 0),
      CONSTRAINT limits_profiles_daily_chk CHECK (daily >= 0),
      CONSTRAINT limits_profiles_monthly_chk CHECK (monthly >= 0)
);

CREATE TABLE IF NOT EXISTS limits.limits_usage_daily (
     owner_id BIGINT NOT NULL,
     day      DATE NOT NULL,
     used     NUMERIC(19,4) NOT NULL DEFAULT 0,

     PRIMARY KEY (owner_id, day),

     CONSTRAINT limits_usage_daily_owner_fk
         FOREIGN KEY (owner_id)
             REFERENCES auth.users(id)
             ON UPDATE CASCADE
             ON DELETE CASCADE,

     CONSTRAINT limits_usage_daily_used_chk CHECK (used >= 0)
);

CREATE TABLE IF NOT EXISTS limits.limits_usage_monthly (
   owner_id     BIGINT NOT NULL,
   month_start  DATE NOT NULL,
   used         NUMERIC(19,4) NOT NULL DEFAULT 0,

   PRIMARY KEY (owner_id, month_start),

   CONSTRAINT limits_usage_monthly_owner_fk
       FOREIGN KEY (owner_id)
           REFERENCES auth.users(id)
           ON UPDATE CASCADE
           ON DELETE CASCADE,

   CONSTRAINT limits_usage_monthly_used_chk CHECK (used >= 0)
);

CREATE INDEX IF NOT EXISTS ix_limits_usage_daily_owner_day
    ON limits.limits_usage_daily(owner_id, day);

CREATE INDEX IF NOT EXISTS ix_limits_usage_monthly_owner_month
    ON limits.limits_usage_monthly(owner_id, month_start);
