
echo "== killing containers == "
docker kill $(docker ps -q)
docker rm $(docker ps -a -q)
docker network prune -f
echo "== killed containers == "

docker-compose -f assets/docker-compose/dev-lite/docker-compose.yml down -v
docker-compose -f assets/docker-compose/dev-lite/docker-compose.yml up -d

sleep 30

sh assets/scripts/cassandra/setup_cassandra.sh
echo "== setup akka tables on Cassandra =="


kill -9 `lsof -t -i:2552`
export SEED_NODES="akka://PersonClassificationServiceReadSide@0.0.0.0:2552"
export CLUSTER_PORT=2552
export MANAGEMENT_PORT=8559
sbt createTables
sbt readside 2>&1 | tee ./readsideLog.txt &

sleep 120

kill -9 `lsof -t -i:2551`
export SEED_NODES="akka://PersonClassificationService@0.0.0.0:2551"
export CLUSTER_PORT=2551
export MANAGEMENT_PORT=8551
sbt pcs/run 2>&1 | tee ./writesideLog.txt &
