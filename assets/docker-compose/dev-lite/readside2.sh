export SEED_NODES="akka://PersonClassificationServiceReadSide@0.0.0.0:2554"
export CLUSTER_PORT=2555
export MANAGEMENT_PORT=8555
export HTTP_PORT=8085
export PROMETHEUS_PORT=5005
export KAMON_STATUS_PAGE=5270
sbt readside/run

