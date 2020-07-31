#!/bin/sh

echo "== stopping up cassandra =="
docker-compose -f assets/docker-compose/vm/docker-compose-cassandra.yml down -v
echo "== cassandra stopped =="

echo "== stopping up kafka =="
docker-compose -f assets/docker-compose/vm/docker-compose-kafka.yml down -v
echo "== kafka stopped =="

echo "== stopping actores =="
docker-compose -f assets/docker-compose/vm/docker-compose.yml down -v
echo "== actores stopped =="
