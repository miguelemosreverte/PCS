import os
import json
from string import Template

obligacionTriTemplate = Template("""
{
  "EV_ID":"${EV_ID}",
  "BOB_SUJ_IDENTIFICADOR":"${BOB_SUJ_IDENTIFICADOR}",
  "BOB_SOJ_IDENTIFICADOR":"${BOB_SOJ_IDENTIFICADOR}",
  "BOB_SOJ_TIPO_OBJETO":"I",
  "BOB_OBN_ID":"${BOB_OBN_ID}",
  "BOB_SALDO":"1",
  "BOB_CUOTA":"10",
  "BOB_ESTADO":null,
  "BOB_FISCALIZADA":"N",
  "BOB_INDICE_INT_PUNIT":null,
  "BOB_INDICE_INT_RESAR":null,
  "BOB_INTERES_PUNIT":null,
  "BOB_INTERES_RESAR":null,
  "BOB_JUI_ID": 1,
  "BOB_PERIODO":"2019",
  "BOB_PLN_ID":null,
  "BOB_PRORROGA":"2019-11-12 00:00:00.0",
  "BOB_TIPO":"tributaria",
  "BOB_TOTAL":null,
  "BOB_CONCEPTO":"701",
  "BOB_IMPUESTO":"600",
  "BOB_VENCIMIENTO":"2019-11-12 00:00:00.0",
  "BOB_CAPITAL":"1",
  "BOB_OTROS_ATRIBUTOS":{
    "BOB_DETALLES":[
      {
        "BOB_MUNICIPIO":null,
        "RULE_NUMBER":"14"
      }
    ]
  }
}
""")

for i in range(0, 1000):
    sujetoId = 1
    objetoId = 1
    obligacionId = i
    deliveryId = i

    obligacionTri = obligacionTriTemplate \
    .substitute(
    EV_ID=deliveryId,
    BOB_SUJ_IDENTIFICADOR=sujetoId,
    BOB_SOJ_IDENTIFICADOR=objetoId,
    BOB_OBN_ID=obligacionId
    )

    print(obligacionTri)
    os.system(f"""echo '{json.dumps(json.loads(obligacionTri))}' | kafkacat -P -b 0.0.0.0:9092 -t DGR-COP-OBLIGACIONES-TRI """)