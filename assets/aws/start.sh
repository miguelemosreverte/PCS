



aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin 099925565557.dkr.ecr.us-west-2.amazonaws.com



git clone https://miguelemosreverte:Alatriste007@github.com/miguelemosreverte/PCS
cd PCS
git checkout AWS

git pull origin AWS

sbt pcs/docker:publishLocal
docker tag pcs/pcs:1.0 099925565557.dkr.ecr.us-west-2.amazonaws.com/pcs-akka:latest
docker push 099925565557.dkr.ecr.us-west-2.amazonaws.com/pcs-akka:latest
export IMAGE=099925565557.dkr.ecr.us-west-2.amazonaws.com/pcs-akka:latest

kubectl apply -f assets/k8s/infra/kafka.yml
kubectl apply -f assets/k8s/infra/cassandra.yml



curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 | bash
helm repo add stable https://kubernetes-charts.storage.googleapis.com
helm install prometheus stable/prometheus-operator --namespace copernico


sleep 60

export kafkaPod=$(kubectl get pod -l "app=kafka" -o jsonpath='{.items[0].metadata.name}')
export createSujetoTri='kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 20 --topic DGR-COP-SUJETO-TRI'
kubectl exec $kafkaPod -- $createSujetoTri
export createObligacionTri='kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 20 --topic DGR-COP-OBLIGACIONES-TRI'
kubectl exec $kafkaPod -- $createObligacionTri

message "Setting up cassandra"
export pod_name=$(kubectl get pod --selector app=cassandra | grep cassandra | cut -d' ' -f 1)

# call setup_cassandra.sh
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/keyspaces/akka.cql

kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/keyspaces/akka.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/keyspaces/akka_snapshot.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/all_persistence_ids.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/messages.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/metadata.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/snapshots.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/tag_scanning.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/tag_views.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/tag_write_progress.cql

kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/cqrs/keyspaces/akka_projection.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/cqrs/tables/offset_store.cql

kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/keyspaces/read_side.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_actividades_sujeto.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_contactos.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_declaraciones_juradas.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_domicilios_objeto.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_domicilios_sujeto.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_exenciones.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_juicios.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_obligaciones.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_planes_pago.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_subastas.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_sujeto.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_sujeto_objeto.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_tramites.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_etapas_procesales.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_param_plan.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_param_recargo.cql

export REPLICAS=2
export PARALELLISM=1024
export IMAGE=099925565557.dkr.ecr.us-west-2.amazonaws.com/pcs-akka:latest
kubectl apply -f assets/k8s/pcs/pcs-rbac.yml
envsubst < assets/k8s/pcs/pcs-deployment.yml | kubectl apply -f -
kubectl apply -f assets/k8s/pcs/pcs-service.yml
kubectl apply -f assets/k8s/pcs/pcs-service-monitor.yml

export kafka_cluster_ip=$(kubectl get svc kafka-internal -ojsonpath='{.spec.clusterIP}')

sbt 'it/runMain generator.KafkaEventProducer '"$kafka_cluster_ip"':29092 DGR-COP-SUJETO-TRI 1 500000 '
sbt 'it/runMain generator.KafkaEventProducer '"$kafka_cluster_ip"':29092 DGR-COP-OBLIGACIONES-TRI 1 500000 '

