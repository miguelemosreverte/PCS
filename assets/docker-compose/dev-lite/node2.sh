
kill -9 `lsof -t -i:2553`
export SEED_NODES="akka://PersonClassificationService@0.0.0.0:2551"
export CLUSTER_PORT=2553
export MANAGEMENT_PORT=8553
export HTTP_PORT=8083
export PROMETHEUS_PORT=5003
sbt pcs/run

