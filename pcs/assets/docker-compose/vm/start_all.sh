#!/bin/sh

echo "== cleaning previous common dependency =="
rm -rf ~/.ivy2/local/weetekio/common_2.13
echo "== common dependency cleaned =="

echo "== creating docker network kafka_copernico_net =="
docker network create kafka_copernico_net --subnet 172.22.0.0/16
echo "== docker network kafka_copernico_net created =="

echo "== starting up cassandra =="
docker-compose -f assets/docker-compose/vm/docker-compose-cassandra.yml up -d
echo "== cassandra started =="

echo "== starting up kafka =="
docker-compose -f assets/docker-compose/vm/docker-compose-kafka.yml up -d
echo "== kafka started =="

echo "== publishing the new docker images =="
# sbt 'common/docker:publishLocal'
# sbt 'pcs/docker:publishLocal'
# sbt 'readside/docker:publishLocal'
sbt docker:publishLocal
echo "== new docker images published =="

echo "== akka cassandra setup =="
sh assets/scripts/cassandra/setup_cassandra.sh

echo "== starting up actores =="
docker-compose -f assets/docker-compose/vm/docker-compose.yml up -d seed # node1 node2
echo "== actores started up =="

echo "== starting up readside =="
docker-compose -f assets/docker-compose/vm/docker-compose.yml up -d readside
echo "== readside started up =="

sleep 20
sh assets/scripts/start_consumers.sh
