
kill -9 `lsof -t -i:2552`
export SEED_NODES="akka://PersonClassificationService@0.0.0.0:2551"
export CLUSTER_PORT=2552
export MANAGEMENT_PORT=8552
export HTTP_PORT=8082
export PROMETHEUS_PORT=5002
sbt pcs/run
