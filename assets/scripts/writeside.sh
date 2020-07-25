docker exec -i cassandra cqlsh < assets/scripts/cassandra/truncate_cassandra.cql
# sh assets/scripts/cassandra/setup_cassandra.sh

kill -9 `lsof -t -i:2551`
export SEED_NODES="akka://PersonClassificationService@0.0.0.0:2551"
export CLUSTER_PORT=2551
export MANAGEMENT_PORT=8551
sleep 60 && sh assets/scripts/start_consumers.sh &
sbt pcs/run
