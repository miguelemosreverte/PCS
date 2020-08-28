
echo "== publishing the new docker images =="
sbt 'pcs/docker:publishLocal'
docker-compose -f assets/docker-compose/docker-compose.yml up -d seed  node1 node2

