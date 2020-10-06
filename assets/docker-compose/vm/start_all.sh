sh assets/environments/vm/infrastructure.sh;

sbt 'pcs/docker:publishLocal'; 
sbt 'readside/docker:publishLocal';
docker-compose -f assets/environments/vm/docker-compose.yml up -d;


sh assets/scripts/wait_ready.sh 8081;
curl -X POST http://0.0.0.0:8081/kafka/start;
sh assets/scripts/wait_ready.sh 8084;
curl -X POST http://0.0.0.0:8084/kafka/start;


pcs.infrastructure.publish_to_kafka DGR-COP-SUJETO-TRI;
pcs.infrastructure.publish_to_kafka DGR-COP-OBLIGACIONES-TRI;
pcs.infrastructure.publish_to_kafka DGR-COP-ACTIVIDADES;
pcs.infrastructure.publish_to_kafka DGR-COP-DOMICILIO-SUJ-TRI;