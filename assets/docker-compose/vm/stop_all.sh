docker-compose -f assets/docker-compose/docker-compose-cassandra.yml down -v;
docker-compose -f assets/docker-compose/docker-compose-elk.yml down -v;
docker-compose -f assets/docker-compose/docker-compose-kafka.yml down -v;
docker-compose -f assets/docker-compose/docker-compose-monitoring.yml down -v;
docker-compose -f assets/docker-compose/docker-compose-scylla.yml down -v;

docker-compose -f assets/environments/vm/docker-compose.yml down -v;