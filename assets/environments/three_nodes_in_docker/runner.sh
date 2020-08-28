
tmux new-window  -n 'local demo'
tmux send-keys -t 1 'echo "Starting up infrastructure"'  Enter
tmux send-keys -t 1 'source aliases.sh' Enter

tmux send-keys -t 1 "\
    sh assets/environments/three_nodes_in_docker/infrastructure.sh; \
    pcs.helper.application.wait_ready 8081; \
    pcs.helper.application.start_consumers; \
    pcs.infrastructure.publish_to_kafka DGR-COP-SUJETO-TRI; \
    pcs.infrastructure.publish_to_kafka DGR-COP-OBLIGACIONES-TRI; \
    pcs.infrastructure.publish_to_kafka DGR-COP-ACTIVIDADES-TRI; \
    pcs.infrastructure.publish_to_kafka DGR-COP-DOMICILIO-SUJ-TRI; \
 " Enter

tmux new-window  -n 'writeside' \; split-window -d \;
tmux send-keys -t 1 'sh assets/environments/three_nodes_in_docker/writeside.sh' Enter
tmux send-keys -t 2 "\
    sh assets/scripts/query_api.sh \
    0.0.0.0:8081/state/sujeto/1
" Enter

tmux new-window  -n 'readside ' \; split-window -d \;
tmux send-keys -t 1 'sh assets/environments/three_nodes_in_docker/readside.sh' Enter
tmux send-keys -t 2 'sh assets/scripts/query_cassandra_table.sh buc_obligaciones' Enter
