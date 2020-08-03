sh assets/docker-compose/vm/stop_all.sh
sh assets/docker-compose/vm/start_all.sh
sleep 100
docker-compose -f assets/docker-compose/vm/docker-compose-kafka.yml exec kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 120 --topic DGR-COP-ACTIVIDADES
curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-ACTIVIDADES

sleep 5; python3 assets/examples/DGR-COP-ACTIVIDADES.py 1
sleep 5; python3 assets/examples/DGR-COP-ACTIVIDADES.py 2
sleep 5; python3 assets/examples/DGR-COP-ACTIVIDADES.py 3
sleep 5; python3 assets/examples/DGR-COP-ACTIVIDADES.py 4
sleep 5; python3 assets/examples/DGR-COP-ACTIVIDADES.py 5
sleep 5; python3 assets/examples/DGR-COP-ACTIVIDADES.py 6
sleep 5; python3 assets/examples/DGR-COP-ACTIVIDADES.py 7
sleep 5; python3 assets/examples/DGR-COP-ACTIVIDADES.py 8
sleep 5; python3 assets/examples/DGR-COP-ACTIVIDADES.py 9
sleep 5; python3 assets/examples/DGR-COP-ACTIVIDADES.py 10
sleep 5; python3 assets/examples/DGR-COP-ACTIVIDADES.py 17