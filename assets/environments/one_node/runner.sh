
tmux new-window  -n 'local demo'
tmux send-keys -t 1 'echo "Starting up infrastructure"'  Enter
tmux send-keys -t 1 'source aliases.sh' Enter

tmux send-keys -t 1 "\
    pcs.demo.local.start.infrastructure; \
    pcs.helper.application.wait_ready 8081; \
    pcs.helper.application.start_consumers; \
    pcs.helper.kafka.publish_obligacion_tri_example \
 " Enter

tmux new-window  -n 'writeside' \; split-window -d \;
tmux send-keys -t 1 'sh assets/environments/one_node/writeside.sh' Enter
tmux send-keys -t 2 "\
    sh assets/scripts/query_api.sh \
    0.0.0.0:8081/state/sujeto/1
" Enter

tmux new-window  -n 'readside ' \; split-window -d \;
tmux send-keys -t 1 'sh assets/environments/one_node/readside.sh' Enter
tmux send-keys -t 2 'sh assets/scripts/query_cassandra_table.sh buc_obligaciones' Enter