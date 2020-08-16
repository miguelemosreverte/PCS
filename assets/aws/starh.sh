kubectl port-forward pod/prometheus-prometheus-prometheus-oper-prometheus-0 9090 &
kubectl port-forward pod/prometheus-grafana-646f8b887d-j9jwr 3000 &

kubectl port-forward pod/pcs-6cf746b44c-667lk 8086:8081
kubectl port-forward pod/pcs-6cf746b44c-727rb 8087:8081
kubectl port-forward pod/pcs-6cf746b44c-7rsfz 8088:8081
kubectl port-forward pod/pcs-6cf746b44c-9kw99 8089:8081
kubectl port-forward pod/pcs-6cf746b44c-vhp7q 8090:8081
pod/pcs-6cf746b44c-667lk                                     0/1     Running   0          22s
pod/pcs-6cf746b44c-727rb                                     0/1     Running   0          22s
pod/pcs-6cf746b44c-7rsfz                                     0/1     Running   0          22s
pod/pcs-6cf746b44c-9kw99                                     0/1     Running   0          22s
pod/pcs-6cf746b44c-vhp7q                                       1/1

curl -X POST http://0.0.0.0:8086/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8087/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8088/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8089/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8090/kafka/start/DGR-COP-SUJETO-TRI
