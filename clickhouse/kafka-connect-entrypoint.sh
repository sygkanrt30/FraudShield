#!/bin/bash
set -e

echo "=== Kafka Connect entrypoint ==="

(
  echo "Waiting for Kafka Connect REST API..."
  for i in $(seq 1 60); do
    if curl -sf http://localhost:8083/ > /dev/null 2>&1; then
      echo "Kafka Connect is up"
      break
    fi
    sleep 2
  done

  if [ -f /setup.sh ]; then
    echo "Running /setup.sh..."
    /setup.sh
  else
    echo "WARNING: /setup.sh not found, skipping connector setup"
  fi
) &

echo "Starting Kafka Connect..."
exec /etc/confluent/docker/run