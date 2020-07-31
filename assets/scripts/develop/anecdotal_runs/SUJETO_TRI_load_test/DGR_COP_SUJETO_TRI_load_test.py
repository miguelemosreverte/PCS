import os

print("""

    Hello there!

    This test will try to run 3 nodes,
    and hit them with 2000 DGR_COP_SUJETO_TRI.json at once.


    Visit the following Grafana dashboard:

    DGR-COP-SUJETO-TRI - Lost Messages

    to see that everything ran correctly.

    You will find relevant info like how many messages were lost!
    Let's hope the number is zero! :)





""")

provisioningFolder = "assets/docker-compose/monitoring/grafana/dashboards/"
dashboard = "DGR-COP-SUJETO-TRI__Lost_Messages.json"
dashboardTemplateLocation = "assets/scripts/develop/anecdotal_runs/SUJETO_TRI_load_test/"
commands = [
"ls",
"cp " + (dashboardTemplateLocation + dashboard) + " " + (provisioningFolder + dashboard),
"sh assets/docker-compose/vm/stop_all.sh",
"sh assets/docker-compose/vm/start_all.sh",
"sleep 120",
"sh assets/scripts/start_consumers.sh",
"python3 assets/examples/DGR-COP-SUJETO-TRI.py 2000",
"rm " + provisioningFolder + dashboard
]

os.system(" && ".join(commands))
