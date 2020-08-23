
### pcs
  ### pcs.infrastructure
    alias pcs.infrastructure.cqlsh="docker exec -it cassandra bash -c 'cqlsh -u cassandra -p cassandra'"

    pcs.infrastructure.publish_to_kafka() {
        if [ -z "$1" ]
        then
          echo "No argument supplied: Try 'publish_to_kafka DGR-COP-SUJETO-TRI' or even better, use pcs.helper.kafka.publish_sujeto_tri_example"
        else
          kafkacat -P -b 0.0.0.0:9092 -t "$1" assets/examples/$1.json
          echo "Published $1 to kafka"
        fi
    }

  ### pcs.helper
    ### pcs.helper.kafka
    alias pcs.helper.kafka.publish_sujeto_tri_example="pcs.infrastructure.publish_to_kafka DGR-COP-SUJETO-TRI"
    alias pcs.helper.kafka.publish_sujeto_ant_example="pcs.infrastructure.publish_to_kafka DGR-COP-SUJETO-ANT"
    alias pcs.helper.kafka.publish_obligacion_tri_example="pcs.infrastructure.publish_to_kafka DGR-COP-OBLIGACIONES-TRI"
    alias pcs.helper.kafka.publish_obligacion_ant_example="pcs.infrastructure.publish_to_kafka DGR-COP-OBLIGACIONES-ANT"
    ### pcs.helper.application
    alias pcs.helper.application.start_consumers="sh assets/scripts/start_consumers.sh"
    alias pcs.helper.application.stop_consumers="sh assets/scripts/stop_consumers.sh"



  ### pcs.install
  pcs.install.tmux.osx() {
    brew install tmux
  }
  pcs.install.tmux.ubuntu() {
    wget -q -O - https://gist.githubusercontent.com/P7h/91e14096374075f5316e/raw/64b011f1bda145dcafe84b5321255d6ed4609c07/tmux__Ubuntu__build_from_source.sh | bash
    source ~/.bashrc
  }

  pcs.demo.local() {

    tmux new-window  -n 'local demo'
    tmux send-keys -t 1 'echo "Starting up infrastructure"'  Enter
    tmux send-keys -t 1 'source aliases.sh' Enter 'pcs.demo.local.start.infrastructure'  Enter

    tmux new-window  -n 'writeside' \; split-window -d \; split-window -h \; split-window -h
    tmux send-keys -t 1 'source aliases.sh' Enter 'pcs.demo.local.start.seed'            Enter
    tmux send-keys -t 2 'source aliases.sh' Enter 'pcs.demo.local.start.node1'           Enter
    tmux send-keys -t 3 'source aliases.sh' Enter 'pcs.demo.local.start.node2'           Enter


    tmux new-window  -n 'readside ' \; split-window -d \; split-window -h \; split-window -h
    tmux send-keys -t 1 'source aliases.sh' Enter 'pcs.demo.local.start.readside1'        Enter
    tmux send-keys -t 2 'source aliases.sh' Enter 'pcs.demo.local.start.readside2'        Enter
    tmux send-keys -t 3 'source aliases.sh' Enter 'pcs.demo.local.start.readside3'        Enter


  }

  alias pcs.demo.local.start.seed="sh assets/docker-compose/dev-lite/seed.sh"
  alias pcs.demo.local.start.node1="sh assets/docker-compose/dev-lite/node1.sh"
  alias pcs.demo.local.start.node2="sh assets/docker-compose/dev-lite/node2.sh"
  alias pcs.demo.local.start.readside1="sh assets/docker-compose/dev-lite/readside1.sh"
  alias pcs.demo.local.start.readside2="sh assets/docker-compose/dev-lite/readside2.sh"
  alias pcs.demo.local.start.readside3="sh assets/docker-compose/dev-lite/readside3.sh"
  alias pcs.demo.local.start.infrastructure="bash assets/docker-compose/dev-lite/infrastructure.sh"

  alias stop_consumers="assets/scripts/stop_consumers.sh"