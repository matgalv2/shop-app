#!/bin/bash
/opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic order-requests --partitions 1 --replication-factor 1
/opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic order-responses --partitions 1 --replication-factor 1