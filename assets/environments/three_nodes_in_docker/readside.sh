
echo "== publishing the new docker images =="
sbt 'readside/docker:publishLocal'
docker-compose -f assets/docker-compose/docker-compose.yml up -d readside1 readside2 readside3
