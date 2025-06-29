#!/bin/bash
/opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic order_create_requests --partitions 1 --replication-factor 1
/opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic order_create_responses --partitions 1 --replication-factor 1