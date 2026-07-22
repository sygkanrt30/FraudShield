#!/bin/bash

# Setup script for Kafka Connect with ClickHouse sink connector

set -e

echo "Waiting for Kafka Connect to start..."
sleep 20

echo "Installing ClickHouse Kafka Connector..."
confluent-hub install --no-prompt clickhouse/clickhouse-kafka-connect:latest

echo "Creating ClickHouse sink connector..."
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "clickhouse-sink-raw-transactions",
    "config": {
      "connector.class": "com.clickhouse.kafka.connect.ClickHouseSinkConnector",
      "tasks.max": "1",
      "topics": "raw-transactions",
      "clickhouse.server.url": "clickhouse:8123",
      "clickhouse.username": "kafka_connect",
      "clickhouse.password": "kafka_connect_pass",
      "clickhouse.database": "transactions",
      "clickhouse.table": "raw_transactions",
      "clickhouse.errors.tolerance": "all",
      "clickhouse.ignore.errors": "true",
      "key.converter": "org.apache.kafka.connect.storage.StringConverter",
      "value.converter": "org.apache.kafka.connect.json.JsonConverter",
      "value.converter.schemas.enable": "false",
      "clickhouse.batch.size": "1000",
      "clickhouse.batch.interval": "1000",
      "clickhouse.retry.on.error": "true",
      "clickhouse.retry.max.attempts": "3"
    }
  }'

echo "Kafka Connect is ready!"