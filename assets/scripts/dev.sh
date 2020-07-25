sh assets/scripts/infrastructure.sh &
sleep 20
kill $(lsof -t -i:2551) > /dev/null 2>&1
sh assets/scripts/writeside.sh &