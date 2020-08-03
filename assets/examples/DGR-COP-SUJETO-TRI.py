import os
import json
import argparse
from string import Template


parser = argparse.ArgumentParser(description='The ammount of messages you want. :) ')
parser.add_argument('messages', metavar='M', type=int, help='The ammount of messages you want. :) ', default=42)
args = parser.parse_args()


sujetoTriTemplate = Template("""
{
  "EV_ID" : "${EV_ID}",
  "SUJ_IDENTIFICADOR" : "${SUJ_IDENTIFICADOR}",
  "SUJ_CAT_SUJ_ID" : null,
  "SUJ_DENOMINACION" : "",
  "SUJ_DFE" : null,
  "SUJ_DIRECCION" : null,
  "SUJ_EMAIL" : null,
  "SUJ_ID_EXTERNO" : null,
  "SUJ_OTROS_ATRIBUTOS" : null,
  "SUJ_RIESGO_FISCAL" : null,
  "SUJ_SITUACION_FISCAL" : null,
  "SUJ_TELEFONO" : null,
  "SUJ_TIPO" : null
}
""")

for i in range(0, args.messages):
    sujetoId = i
    deliveryId = i

    sujetoTri = sujetoTriTemplate \
    .substitute(
    EV_ID=deliveryId,
    SUJ_IDENTIFICADOR=sujetoId
    )

    print(sujetoTri)

    os.system(f"""echo '{json.dumps(json.loads(sujetoTri))}' | kafkacat -P -b 0.0.0.0:9092 -t DGR-COP-SUJETO-TRI """)
