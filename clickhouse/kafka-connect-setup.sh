#!/bin/bash
set -e

echo "Installing ClickHouse Kafka Connector..."
confluent-hub install --no-prompt clickhouse/clickhouse-kafka-connect:latest

echo "Creating ClickHouse sink connector..."
curl -X PUT http://localhost:8083/connectors/clickhouse-sink-raw-transactions/config \
  -H "Content-Type: application/json" \
  -d '{
    "connector.class": "com.clickhouse.kafka.connect.ClickHouseSinkConnector",
    "tasks.max": "1",
    "topics": "raw-transactions",

    "clickhouse.server.url": "clickhouse:8123",
    "clickhouse.username": "kafka_connect",
    "clickhouse.password": "kafka_connect_pass",
    "clickhouse.database": "transactions",
    "clickhouse.table": "transactions",

    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false",

    "clickhouse.batch.size": "1000",
    "clickhouse.batch.interval": "1000",
    "clickhouse.retry.on.error": "true",
    "clickhouse.retry.max.attempts": "5",

    "errors.tolerance": "all",
    "errors.log.enable": "true",
    "errors.log.include.messages": "true",
    "errors.deadletterqueue.topic.name": "raw-transactions-dlq",
    "errors.deadletterqueue.topic.replication.factor": 1,
    "errors.deadletterqueue.context.headers.enable": "true"
  }'

echo "Kafka Connect setup finished!"