package consumers.no_registral.obligacion.application.cqrs.queries

import consumers.no_registral.obligacion.application.entities.ObligacionResponses.GetObligacionResponse
import consumers.no_registral.obligacion.application.entities.{ObligacionQueries, ObligacionResponses}
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import cqrs.untyped.query.QueryHandler.SyncQueryHandler

import scala.util.{Success, Try}

class ObligacionGetStateHandler(actor: ObligacionActor) extends SyncQueryHandler[ObligacionQueries.GetStateObligacion] {

  override def handle(
      query: ObligacionQueries.GetStateObligacion
  ): Try[ObligacionResponses.GetObligacionResponse] = {
    val replyTo = actor.context.sender()
    val response = GetObligacionResponse(
      actor.state.saldo,
      actor.state.vencida,
      actor.state.fechaUltMod,
      actor.state.registro,
      actor.state.detallesObligacion,
      actor.state.exenta,
      actor.state.porcentajeExencion.getOrElse(0),
      actor.state.fechaVencimiento,
      actor.state.juicioId
    )
    log.info(s"[${actor.persistenceId}] GetState | $response")
    replyTo ! response
    Success(response)
  }
}
