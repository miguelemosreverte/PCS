kubectl port-forward pod/prometheus-prometheus-prometheus-oper-prometheus-0 9090 &
kubectl port-forward pod/prometheus-grafana-646f8b887d-j9jwr 3000 &

kubectl port-forward pod/pcs-7c4985f8cd-69j8h 8081:8081 &
kubectl port-forward pod/pcs-7c4985f8cd-m296z 8082:8081 &
kubectl port-forward pod/pcs-7c4985f8cd-tzjdz 8083:8081 &

sleep 10

curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8082/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8083/kafka/start/DGR-COP-SUJETO-TRI