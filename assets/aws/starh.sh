kubectl port-forward pod/prometheus-grafana-646f8b887d-9q7qz  3000



kubectl port-forward pod/pcs-6945dd469f-b765c  8081:8081
kubectl port-forward pod/pcs-6945dd469f-lhnlf 8082:8081


curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8082/kafka/start/DGR-COP-SUJETO-TRI