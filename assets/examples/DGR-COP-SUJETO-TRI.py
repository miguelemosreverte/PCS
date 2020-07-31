import os
import json
from string import Template

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

for i in range(0, 2000):
    sujetoId = i
    deliveryId = i

    sujetoTri = sujetoTriTemplate \
    .substitute(
    EV_ID=deliveryId,
    SUJ_IDENTIFICADOR=sujetoId
    )

    print(sujetoTri)

    os.system(f"""echo '{json.dumps(json.loads(sujetoTri))}' | kafkacat -P -b 0.0.0.0:9092 -t DGR-COP-SUJETO-TRI """)