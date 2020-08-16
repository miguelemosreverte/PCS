kubectl port-forward pod/prometheus-prometheus-prometheus-oper-prometheus-0 9090 &
kubectl port-forward pod/prometheus-grafana-646f8b887d-j9jwr 3000 &

kubectl port-forward pod/pcs-6d4849b8df-flspl 8086:8081
kubectl port-forward pod/pcs-6d4849b8df-vtszn 8087:8081
kubectl port-forward pod/pcs-6d4849b8df-vztv8 8088:8081


curl -X POST http://0.0.0.0:8086/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8087/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8088/kafka/start/DGR-COP-SUJETO-TRI