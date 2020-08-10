export SEED_NODES="akka://PersonClassificationServiceReadSide@0.0.0.0:2554"
export CLUSTER_PORT=2556
export MANAGEMENT_PORT=8556
export HTTP_PORT=8086
export PROMETHEUS_PORT=5006
export KAMON_STATUS_PAGE=5271
sbt readside/run

