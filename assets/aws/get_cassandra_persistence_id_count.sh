kubectl exec -it \
$(kubectl get pod -l app=cassandra -o jsonpath='{.items[0].metadata.name}') \
 -- cqlsh -e 'describe tables';

kubectl exec -it \
$(kubectl get pod -l app=cassandra -o jsonpath='{.items[0].metadata.name}') \
 -- cqlsh -e 'select count(*) from akka.all_persistence_ids';
