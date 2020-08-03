# docker exec -i cassandra cqlsh < assets/scripts/cassandra/truncate_cassandra.cql
# sh assets/scripts/cassandra/setup_cassandra.sh

docker-compose -f assets/docker-compose/dev-lite/docker-compose.yml exec kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 120 --topic DGR-COP-ACTIVIDADES


kill -9 `lsof -t -i:2551`
export SEED_NODES="akka://PersonClassificationService@0.0.0.0:2551"
export CLUSTER_PORT=2551
export MANAGEMENT_PORT=8551
export HTTP_PORT=8081
sbt pcs/run


kill -9 `lsof -t -i:2552`
export SEED_NODES="akka://PersonClassificationService@0.0.0.0:2551"
export CLUSTER_PORT=2552
export MANAGEMENT_PORT=8552
export HTTP_PORT=8082
sbt pcs/run

