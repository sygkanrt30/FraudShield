CREATE DATABASE IF NOT EXISTS transactions;

USE transactions;

CREATE TABLE IF NOT EXISTS transactions (
    txId String,
    fromClientId UInt64,
    toClientId UInt64,
    amount Decimal(18,2),
    currency String DEFAULT 'RUB',
    timestamp DateTime,
    status String DEFAULT 'PENDING',
    isFraud Boolean DEFAULT false,
    riskScore Float32 DEFAULT 0.0,
    fraudReason String DEFAULT '',
    processedAt DateTime DEFAULT now(),
    kafkaOffset UInt64
) ENGINE = ReplacingMergeTree()
PARTITION BY toYYYYMM(timestamp)
ORDER BY (fromClientId, toClientId, timestamp)
SETTINGS index_granularity = 8192;

CREATE TABLE IF NOT EXISTS fraud_metrics (
    clientId UInt64,
    metricDate Date,
    totalSentToHubs Float64,
    txCountToHubs UInt32,
    avgChequeToHubs Float64,
    totalSent Float64,
    txCount UInt32,
    avgCheque Float64,
    weeklyGrowth Float64,
    newRecipientsCount UInt32,
    calculatedAt DateTime DEFAULT now()
) ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(metricDate)
ORDER BY (clientId, metricDate)
SETTINGS index_granularity = 8192;


CREATE MATERIALIZED VIEW IF NOT EXISTS daily_transaction_summary
ENGINE = SummingMergeTree()
ORDER BY (date, currency)
POPULATE
AS SELECT
    toDate(timestamp) as date,
    currency,
    count() as transaction_count,
    sum(amount) as total_amount,
    avg(amount) as avg_amount,
    countIf(status = 'BLOCKED') as blocked_count,
    countIf(isFraud = true) as fraud_count,
    sumIf(amount, isFraud = true) as fraud_amount,
    countIf(status = 'PENDING') as pending_count,
    countIf(status = 'CHECKED') as checked_count
FROM transactions
GROUP BY date, currency;

ALTER TABLE transactions ADD INDEX idx_tx_id txId TYPE bloom_filter GRANULARITY 4;
ALTER TABLE transactions ADD INDEX idx_status status TYPE set(10) GRANULARITY 4;
ALTER TABLE transactions ADD INDEX idx_fraud_reason fraudReason TYPE bloom_filter GRANULARITY 4;

OPTIMIZE TABLE transactions FINAL;
OPTIMIZE TABLE fraud_metrics FINAL;

SELECT '=== Initialization Complete ===' as message;
SHOW TABLES;