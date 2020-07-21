
export SEED_NODES="akka://PersonClassificationServiceReadSide@0.0.0.0:2552"
export CLUSTER_PORT=2552
export MANAGEMENT_PORT=8559
export HTTP_PORT=8082

sh assets/scripts/cassandra/setup_cassandra.sh

sbt readside
