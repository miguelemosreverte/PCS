kubectl port-forward pod/prometheus-grafana-646f8b887d-z58sm  3000


kubectl port-forward pod/pcs-6945dd469f-4264d  8081:8081
kubectl port-forward pod/pcs-6945dd469f-qw4rx 8082:8081


curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8082/kafka/start/DGR-COP-SUJETO-TRI