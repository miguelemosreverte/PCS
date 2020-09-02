
ITER=0
for $cassandra_pod_name in $(kubectl get pods -o name --selector app=cassandra ); do
  ITER=$(expr $ITER + 1)
  # call setup_cassandra.sh
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/keyspaces/akka.cql

  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/keyspaces/akka.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/keyspaces/akka_snapshot.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/all_persistence_ids.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/messages.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/metadata.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/snapshots.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/tag_scanning.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/tag_views.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/tag_write_progress.cql

  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/infrastructure/cqrs/keyspaces/akka_projection.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/infrastructure/cqrs/tables/offset_store.cql

  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/domain/read_side/keyspaces/read_side.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_actividades_sujeto.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_contactos.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_declaraciones_juradas.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_domicilios_objeto.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_domicilios_sujeto.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_exenciones.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_juicios.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_obligaciones.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_planes_pago.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_subastas.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_sujeto.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_sujeto_objeto.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_tramites.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_etapas_procesales.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_param_plan.cql
  kubectl exec -i $cassandra_pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_param_recargo.cql
  done