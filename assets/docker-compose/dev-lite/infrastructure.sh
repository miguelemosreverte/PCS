# needed to rebuild provisioning
docker-compose -f assets/docker-compose/dev-lite/docker-compose.yml build grafana
docker-compose -f assets/docker-compose/dev-lite/docker-compose.yml up -d

checkCassandra() {
  docker exec cassandra cqlsh -e 'describe tables'
}

while ! checkCassandra; do
    sleep 1
done

sh assets/scripts/cassandra/setup_cassandra.sh

docker-compose -f assets/docker-compose/dev-lite/docker-compose.yml exec kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 120 --topic DGR-COP-ACTIVIDADES
docker-compose -f assets/docker-compose/dev-lite/docker-compose.yml exec kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 120 --topic DGR-COP-SUJETO-TRI
docker-compose -f assets/docker-compose/dev-lite/docker-compose.yml exec kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 120 --topic DGR-COP-OBLIGACIONES-TRI


echo "Checking that writeside node 1 is up..."
echo "by using 'curl 0.0.0.0:8081/api/system/status'"
while ! curl 0.0.0.0:8081/api/system/status --silent; do
    sleep 1
done
echo "Writeside node 1 is up."


echo "Checking that writeside node 2 is up..."
echo "by using 'curl 0.0.0.0:8082/api/system/status'"
while ! curl 0.0.0.0:8082/api/system/status --silent; do
    sleep 1
done
echo "Writeside node 2 is up."

echo "Checking that writeside node 3 is up..."
echo "by using 'curl 0.0.0.0:8083/api/system/status'"
while ! curl 0.0.0.0:8083/api/system/status --silent; do
    sleep 1
done
echo "Writeside node 3 is up."

echo "Starting consumers at node 1, node 2, and node 3"
sh assets/scripts/start_consumers.sh

echo "To send kafka messages for writeside to consume:"
echo "kafkacat -P -b 0.0.0.0:9092 -t DGR-COP-SUJETO-TRI assets/DGR-COP-SUJETO-TRI.json"
echo "or"
echo "source aliases.sh; pcs.infrastructure.publish_to_kafka DGR-COP-SUJETO-TRI"
echo "or"
echo "source aliases.sh; pcs.helper.kafka.publish_sujeto_tri_example"