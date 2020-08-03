import os
import json
import argparse
from string import Template


parser = argparse.ArgumentParser(description='The ammount of messages you want. :) ')
parser.add_argument('messages', metavar='M', type=int, help='The ammount of messages you want. :) ', default=42)
args = parser.parse_args()


actividadSujetoTemplate = Template("""

{
  "EV_ID" : "${EV_ID}",
  "BAT_SUJ_IDENTIFICADOR": "${SUJETO_ID}",
  "BAT_ATD_ID": "${ACTIVIDAD_SUJETO_ID}",
  "BAT_DESCRIPCION": "PERCEPCIONES ART.193 DECRETO 1205/2015",
  "BAT_FECHA_INICIO": "2016-06-01 00:00:00.0",
  "BAT_FECHA_FIN": null,
  "BAT_OTROS_ATRIBUTOS": {},
  "BAT_REFERENCIA": "950976",
  "BAT_TIPO": "AG.RETENCION Y PERCEPCION"
}
""")

for i in range(1, args.messages):
    sujetoId = i
    deliveryId = i

    actividadSujeto = actividadSujetoTemplate \
    .substitute(
    EV_ID=deliveryId,
    SUJETO_ID=sujetoId,
    ACTIVIDAD_SUJETO_ID=sujetoId
    )

    print(actividadSujeto)

    os.system(f"""echo '{json.dumps(json.loads(actividadSujeto))}' | kafkacat -P -b 0.0.0.0:9092 -t DGR-COP-ACTIVIDADES """)
