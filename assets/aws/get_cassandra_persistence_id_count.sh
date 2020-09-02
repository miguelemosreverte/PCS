#!/bin/bash

# this forces usage of bash because for loop is bash-only
# in case this script is called using sh, it will re-call itself using bash.
if [ ! "$BASH_VERSION" ] ; then
    exec /bin/bash "$0" "$@"
fi

ITER=0
for cassandra_pod_name in $(kubectl get pods -o name --selector app=cassandra ); do
  ITER=$(expr $ITER + 1)
  kubectl exec -it $cassandra_pod_name -- cqlsh -e 'select count(*) from akka.all_persistence_ids'
  kubectl exec -it $cassandra_pod_name -- cqlsh -e 'select count(*) from akka.messages'
  done
