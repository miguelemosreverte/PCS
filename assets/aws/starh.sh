
export grafanaPod=$(kubectl get pod -l app.kubernetes.io/name=grafana -o jsonpath="{.items[0].metadata.name}")
kubectl port-forward $grafanaPod  3000

export akkaPod1=$(kubectl get pod -l app=pcs-cluster -o jsonpath="{.items[0].metadata.name}")
kubectl port-forward $akkaPod1  8081:8081

export akkaPod2=$(kubectl get pod -l app=pcs-cluster -o jsonpath="{.items[1].metadata.name}")
kubectl port-forward $akkaPod2 8082:8081


sleep 10
curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8082/kafka/start/DGR-COP-SUJETO-TRI
curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-OBLIGACIONES-TRI
curl -X POST http://0.0.0.0:8082/kafka/start/DGR-COP-OBLIGACIONES-TRI