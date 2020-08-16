kubectl port-forward pod/prometheus-grafana-646f8b887d-kjzf8 3000 &

kubectl port-forward pod/pcs-7c4985f8cd-b8jfs 8081:8081 &
kubectl port-forward pod/pcs-7c4985f8cd-bfbgk 8082:8081 &
kubectl port-forward pod/pcs-7c4985f8cd-j7ltb 8083:8081 &
kubectl port-forward pod/pcs-7c4985f8cd-pxkvq 8084:8081 &
kubectl port-forward pod/pcs-7c4985f8cd-qv4sd 8085:8081 &
kubectl port-forward pod/pcs-7c4985f8cd-rsjbb 8086:8081 &
kubectl port-forward pod/pcs-7c4985f8cd-zhf5v 8087:8081 &

sleep 10

curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8082/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8083/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8084/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8085/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8086/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8087/kafka/start/DGR-COP-SUJETO-TRI