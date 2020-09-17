package consumers.no_registral.obligacion.infrastructure.dependency_injection

import akka.actor.Props
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import com.typesafe.config.Config
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.obligacion.application.cqrs.commands._
import consumers.no_registral.obligacion.application.cqrs.queries.ObligacionGetStateHandler
import consumers.no_registral.obligacion.application.entities.ObligacionMessage.ObligacionMessageRoots
import consumers.no_registral.obligacion.application.entities.{
  ObligacionCommands,
  ObligacionExternalDto,
  ObligacionQueries
}
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionPersistedSnapshot
import consumers.no_registral.obligacion.domain.{ObligacionEvents, ObligacionState}
import cqrs.base_actor.untyped.PersistentBaseActor
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer
import monitoring.Monitoring

class ObligacionActor(requirements: MonitoringAndMessageProducer)
    extends PersistentBaseActor[ObligacionEvents, ObligacionState](requirements.monitoring) {

  var state = ObligacionState()

  override def setupHandlers(): Unit = {
    queryBus.subscribe[ObligacionQueries.GetStateObligacion](new ObligacionGetStateHandler(this).handle)
    commandBus.subscribe[ObligacionCommands.ObligacionUpdateFromDto](new ObligacionUpdateFromDtoHandler(this).handle)
    commandBus.subscribe[ObligacionCommands.ObligacionUpdateExencion](new ObligacionUpdateExencionHandler(this).handle)
    commandBus.subscribe[ObligacionCommands.ObligacionRemove](new ObligacionRemoveHandler(this).handle)
    commandBus.subscribe[ObligacionCommands.DownObligacion](new DownObligacionHandler(this).handle)
  }

  def informParent(cmd: ObligacionCommands): Unit = {
    context.parent ! ObjetoCommands.ObjetoUpdateFromObligacion(
      cmd.deliveryId,
      cmd.sujetoId,
      cmd.objetoId,
      cmd.tipoObjeto,
      cmd.obligacionId,
      state.saldo,
      state.exenta,
      state.porcentajeExencion
    )
  }

  def informBajaToParent(cmd: ObligacionCommands): Unit = {
    context.parent ! ObjetoCommands.ObjetoUpdateFromSetBajaObligacion(
      cmd.deliveryId,
      cmd.sujetoId,
      cmd.objetoId,
      cmd.tipoObjeto,
      cmd.obligacionId
    )
  }

  import consumers.no_registral.obligacion.infrastructure.json._
  def persistSnapshot()(handler: () => Unit): Unit = {
    val ids = ObligacionMessageRoots.extractor(persistenceId)

    val event = ObligacionPersistedSnapshot(
      deliveryId = lastDeliveryId,
      sujetoId = ids.sujetoId,
      objetoId = ids.objetoId,
      tipoObjeto = ids.tipoObjeto,
      obligacionId = ids.obligacionId,
      registro = state.registro,
      exenta = state.exenta,
      porcentajeExencion = state.porcentajeExencion.getOrElse(0),
      saldo = state.saldo
    )
    import serialization.encode
    requirements.messageProducer.produce(
      data = Seq(
        KafkaKeyValue(
          persistenceId,
          encode(event)
        )
      ),
      topic = "ObligacionPersistedSnapshot"
    )(_ => ())
  }
}

object ObligacionActor {
  def props(requirements: MonitoringAndMessageProducer): Props =
    Props(new ObligacionActor(requirements))
}
