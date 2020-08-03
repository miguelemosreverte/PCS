docker-compose -f assets/docker-compose/dev-lite/docker-compose.yml down -v

kill -9 `lsof -t -i:2551`
kill -9 `lsof -t -i:2552`
kill -9 `lsof -t -i:2553`
