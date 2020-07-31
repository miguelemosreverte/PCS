import os
from grafanalib.core import (
    Alert, AlertCondition, Dashboard, Graph,
    GreaterThan, OP_AND, OPS_FORMAT, Row, RTYPE_SUM, MILLISECONDS_FORMAT,
    SHORT_FORMAT, single_y_axis, Target, TimeRange, YAxes, YAxis
)


def graph(title, expression, format):
    return Graph(
        title=title,
        dataSource='prometheusdata',
        targets=[
            Target(
                expr=expression,
            )
        ],
        yAxes=YAxes(
            YAxis(format=format)
        )
    )

dashboards_rows_per_service = os.environ['dashboards_rows_per_service']
dashboard = Dashboard(
        title=os.environ['dashboard_title'],
        rows=[
            Row(
                title = os.environ['serviceComponentTitle' + str(row)],
                showTitle = True,
                panels=[
                    graph(
                        title='request',
                        expression=os.environ['requests_PromQL_expression' + str(row)],
                        format=OPS_FORMAT
                    ),
                    graph(
                        title='errors',
                        expression=os.environ['errors_PromQL_expression'+  str(row)],
                        format=OPS_FORMAT
                    ),
                    graph(
                        title='latency',
                        expression=os.environ['duration_PromQL_expression'+  str(row)],
                        format=MILLISECONDS_FORMAT
                    )
                ])
                for row in range(0, int(dashboards_rows_per_service))
        ],
    ).auto_panel_ids()

print(dashboard)
