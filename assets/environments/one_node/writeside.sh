export SEED_NODES="akka://PersonClassificationService@0.0.0.0:2551"
export CLUSTER_PORT=2551
export MANAGEMENT_PORT=8551
export HTTP_PORT=8081
export PROMETHEUS_PORT=5001
export KAMON_STATUS_PAGE=5266
sbt pcs/run

