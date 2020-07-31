import api.Utils.Transformation
import api.actor_transaction.ActorTransaction
import design_principles.actor_model.mechanism.QueryStateAPI

import sys.process._

object GrafanaSetup extends App {

  import GrafanaProvisioning._

  def getServiceName(canonicalName: String) =
    canonicalName.split('.').init.init.init.tail.tail.mkString(".")
  def getServiceComponentName(canonicalName: String) =
    canonicalName.split('.').last
  def extractNames(canonicalName: String): (String, String) =
    (getServiceName(canonicalName), Transformation to_underscore getServiceComponentName(canonicalName))

  val controllers: Seq[RedMetric] = utils.Inference
    .getSubtypesOf[QueryStateAPI]()
    .map(_.getCanonicalName)
    .map(extractNames)
    .map {
      case (serviceName, serviceComponentName) =>
        RedMetric(
          serviceName,
          serviceComponentName,
          s"controller-$serviceComponentName-request",
          s"controller-$serviceComponentName-critical",
          s"controller-$serviceComponentName-latency"
        )
    }
    .toSeq

  val actorTransactions: Seq[RedMetric] = utils.Inference
    .getSubtypesOf[ActorTransaction[_]]()
    .map(_.getCanonicalName)
    .map(extractNames)
    .map {
      case (serviceName, serviceComponentName) =>
        RedMetric(
          serviceName,
          serviceComponentName,
          s"actor-transaction-$serviceComponentName-request",
          s"actor-transaction-$serviceComponentName-error",
          s"actor-transaction-$serviceComponentName-latency"
        )
    }
    .toSeq

  val services = (controllers ++ actorTransactions)
    .groupBy(_.serviceName)
    .foreach {
      case (serviceName, redMetrics) =>
        GrafanaProvisioning.createRedDashboardsJson(redMetrics)
    }

  object GrafanaProvisioning {

    case class RedMetric(
        serviceName: String,
        serviceComponentName: String,
        requests: String,
        errors: String,
        duration: String
    ) {

      object PromQL {
        def requestsExpression = s"""copernico_counters_total{entity="$requests"} """

        def errorsExpression = s"""copernico_counters_total{entity="$errors"} """

        def latencyExpression = s"""copernico_histograms_bucket{le="100000.0",entity="$duration"} """
      }

    }

    def createRedDashboardJson(redMetric: RedMetric): Unit = {
      val dashboardTitle = redMetric.serviceName
      val grafana = "assets/docker-compose/monitoring/grafana/"
      val grafanaDashboards = grafana + "dashboards/"
      val grafanaProvisioning = grafana + "provisioning/"
      val dashboardGenerator = grafanaProvisioning + "grafanalib/python/RED_dashboard.py"
      val dashboardGeneratorRunner = grafanaProvisioning + "grafanalib/create_dashboard.sh"
      val output = grafanaDashboards + dashboardTitle + ".json"

      Process(
        Seq("bash", dashboardGeneratorRunner),
        None,
        "DASHBOARD_JSON_READY_FOR_GRAFANA_PROVISIONING" -> output,
        "RED_DASHBOARD_GENERATOR" -> dashboardGenerator,
        "dashboard_title" -> redMetric.serviceName,
        "requests_PromQL_expression" -> redMetric.PromQL.requestsExpression,
        "errors_PromQL_expression" -> redMetric.PromQL.errorsExpression,
        "duration_PromQL_expression" -> redMetric.PromQL.latencyExpression
      ).!

    }

    def createRedDashboardsJson(redMetrics: Seq[RedMetric]): Unit = {
      val dashboardTitle = redMetrics.head.serviceName
      val grafana = "assets/docker-compose/monitoring/grafana/"
      val grafanaDashboards = grafana + "dashboards/"
      val grafanaProvisioning = grafana + "provisioning/"
      val dashboardGenerator = grafanaProvisioning + "grafanalib/python/RED_dashboards.py"
      val dashboardGeneratorRunner = grafanaProvisioning + "grafanalib/create_dashboard.sh"
      val output = grafanaDashboards + dashboardTitle + ".json"
      val aa = redMetrics.zipWithIndex.flatMap {
        case (redMetric, i) =>
          Seq(
            s"serviceComponentTitle$i" -> redMetric.serviceComponentName,
            s"requests_PromQL_expression$i" -> redMetric.PromQL.requestsExpression,
            s"errors_PromQL_expression$i" -> redMetric.PromQL.errorsExpression,
            s"duration_PromQL_expression$i" -> redMetric.PromQL.latencyExpression
          )
      }

      Process(
        Seq("bash", dashboardGeneratorRunner),
        None,
        Seq(
          "DASHBOARD_JSON_READY_FOR_GRAFANA_PROVISIONING" -> output,
          "RED_DASHBOARD_GENERATOR" -> dashboardGenerator,
          "dashboards_rows_per_service" -> redMetrics.size.toString,
          "dashboard_title" -> dashboardTitle
        ) ++ aa: _*
      ).!
    }
  }

}
