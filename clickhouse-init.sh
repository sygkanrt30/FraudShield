#!/bin/bash

# Initialization script for ClickHouse database
# Creates database schema, tables, views, and users

set -e

echo "========================================="
echo "ClickHouse Database Initialization"
echo "========================================="

echo "Waiting for ClickHouse to start..."
sleep 10

echo "Creating database and tables..."
clickhouse-client \
  --host "${CLICKHOUSE_HOST:-clickhouse}" \
  --port "${CLICKHOUSE_PORT:-9000}" \
  --user "${CLICKHOUSE_USER:-default}" \
  --password "${CLICKHOUSE_PASSWORD:-}" \
  --multiquery < /init.sql

echo "========================================="
echo "Verifying created tables..."
clickhouse-client \
  --host "${CLICKHOUSE_HOST:-clickhouse}" \
  --port "${CLICKHOUSE_PORT:-9000}" \
  --user "${CLICKHOUSE_USER:-default}" \
  --password "${CLICKHOUSE_PASSWORD:-}" \
  --query "SHOW TABLES FROM ${CLICKHOUSE_DB:-transactions}"

echo "========================================="
echo "Checking transactions table structure..."
clickhouse-client \
  --host "${CLICKHOUSE_HOST:-clickhouse}" \
  --port "${CLICKHOUSE_PORT:-9000}" \
  --user "${CLICKHOUSE_USER:-default}" \
  --password "${CLICKHOUSE_PASSWORD:-}" \
  --query "DESCRIBE TABLE ${CLICKHOUSE_DB:-transactions}.transactions"

echo "========================================="
echo "Checking fraud_metrics table structure..."
clickhouse-client \
  --host "${CLICKHOUSE_HOST:-clickhouse}" \
  --port "${CLICKHOUSE_PORT:-9000}" \
  --user "${CLICKHOUSE_USER:-default}" \
  --password "${CLICKHOUSE_PASSWORD:-}" \
  --query "DESCRIBE TABLE ${CLICKHOUSE_DB:-transactions}.fraud_metrics"

echo "========================================="
echo "Checking users..."
clickhouse-client \
  --host "${CLICKHOUSE_HOST:-clickhouse}" \
  --port "${CLICKHOUSE_PORT:-9000}" \
  --user "${CLICKHOUSE_USER:-default}" \
  --password "${CLICKHOUSE_PASSWORD:-}" \
  --query "SHOW USERS"

echo "========================================="
echo "Initialization completed successfully!"
echo "========================================="
echo ""
echo "Summary:"
echo "  - Database: ${CLICKHOUSE_DB:-transactions}"
echo "  - Tables: transactions, fraud_metrics"
echo "  - Materialized Views: daily_transaction_summary, client_activity_summary"
echo "  - Users: kafka_connect, fraud_detector, admin_api, metrics_worker"
echo "========================================="