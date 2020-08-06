# needed to rebuild provisioning
docker-compose -f assets/docker-compose/dev-lite/docker-compose.yml build grafana
docker-compose -f assets/docker-compose/dev-lite/docker-compose.yml up -d

sleep 30

sh assets/scripts/cassandra/setup_cassandra.sh

docker-compose -f assets/docker-compose/dev-lite/docker-compose.yml exec kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 120 --topic DGR-COP-ACTIVIDADES
docker-compose -f assets/docker-compose/dev-lite/docker-compose.yml exec kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 120 --topic DGR-COP-SUJETO-TRI
docker-compose -f assets/docker-compose/dev-lite/docker-compose.yml exec kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 120 --topic DGR-COP-OBLIGACIONES-TRI
