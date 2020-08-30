curl -X POST -H "Content-Type: application/json" -d '{"admin":"prom-operator"}' http://admin:prom-operator@localhost:3000/api/orgs
export AUTH9=$(curl -X POST -H "Content-Type: application/json" -d '{"name":"apikeycurl9", "role": "Admin"}' http://admin:prom-operator@localhost:3000/api/auth/keys)
export KEY9=$(echo $AUTH9 | jq '.key')
echo $AUTH9
echo $KEY9

createDashboard() {
  curl -X POST --insecure -H "Authorization: Bearer $KEY9" -H "Content-Type: application/json" -d '{
    "dashboard": '"$(cat $1)"',
    "overwrite": true
  }' http://localhost:3000/api/dashboards/db
}

createDashboard "assets/docker-compose/grafana/dashboards/DGR-COP-SUJETO-TRI.json"