# needed to rebuild provisioning

echo "== creating docker network kafka_copernico_net =="
docker network create kafka_copernico_net --subnet 172.22.0.0/16
echo "== docker network kafka_copernico_net created =="

echo "== starting up cassandra =="
docker-compose -f assets/docker-compose/docker-compose-cassandra.yml up -d
echo "== cassandra started =="

checkCassandra() {
  docker exec cassandra cqlsh -e 'describe tables' > /dev/null 2>&1
}

while ! checkCassandra; do
    sleep 1
done

sh assets/scripts/cassandra/setup_cassandra.sh
