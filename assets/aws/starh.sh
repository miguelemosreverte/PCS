kubectl port-forward pod/prometheus-prometheus-prometheus-oper-prometheus-0 9090 &
kubectl port-forward pod/prometheus-grafana-646f8b887d-j9jwr 3000 &

kubectl port-forward pod/pcs-6d4849b8df-29lc4 8086:8081
kubectl port-forward pod/pcs-6d4849b8df-f98kz 8087:8081
kubectl port-forward pod/pcs-6d4849b8df-k5jzz 8088:8081
pod/pcs-6d4849b8df-29lc4                                     0/1     Running   0          8s
pod/pcs-6d4849b8df-f98kz                                     0/1     Running   0          8s
pod/pcs-6d4849b8df-k5jzz

curl -X POST http://0.0.0.0:8086/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8087/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8088/kafka/start/DGR-COP-SUJETO-TRI