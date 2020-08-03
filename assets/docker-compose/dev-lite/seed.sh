

kill -9 `lsof -t -i:2551`
export SEED_NODES="akka://PersonClassificationService@0.0.0.0:2551"
export CLUSTER_PORT=2551
export MANAGEMENT_PORT=8551
export HTTP_PORT=8081
sbt pcs/run

