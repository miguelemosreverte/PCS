
echo "== killing containers == "
docker kill $(docker ps -q)
docker rm $(docker ps -a -q)
docker network prune -f
echo "== killed containers == "

docker-compose -f assets/docker-compose/dev-lite/docker-compose.yml down -v
docker-compose -f assets/docker-compose/dev-lite/docker-compose.yml up -d

sleep 30

sh assets/scripts/cassandra/setup_cassandra.sh


