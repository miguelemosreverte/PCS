package consumers.no_registral.sujeto.infrastructure.dependency_injection

import akka.ActorRefMap
import akka.actor.{ActorRef, Props}
import akka.entity.ShardedEntity.{NoRequirements, ShardedEntityNoRequirements}
import consumers.no_registral.objeto.application.entities.ObjetoMessage
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.obligacion.application.entities.ObligacionMessage
import consumers.no_registral.sujeto.application.cqrs.commands.{
  SujetoSetBajaFromObjetoHandler,
  SujetoUpdateFromAntHandler,
  SujetoUpdateFromObjetoHandler,
  SujetoUpdateFromTriHandler
}
import consumers.no_registral.sujeto.application.cqrs.queries.GetStateSujetoHandler
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.application.entity.{SujetoCommands, SujetoQueries}
import consumers.no_registral.sujeto.domain.SujetoEvents.SujetoSnapshotPersisted
import consumers.no_registral.sujeto.domain.{SujetoEvents, SujetoState}
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor.{SujetoActorRefMap, SujetoTags}
import cqrs.PersistentBaseActor
import utils.implicits.StringT._

class SujetoActor(objetoActorProps: Props = ObjetoActor.props) extends PersistentBaseActor[SujetoEvents, SujetoState] {

  var state = SujetoState()

  val objetos = new SujetoActorRefMap(
    {
      case (sujetoId, objetoId, tipoObjeto) =>
        val objetoAggregateRoot = ObjetoMessageRoots(sujetoId, objetoId, tipoObjeto).toString
        context.actorOf(objetoActorProps, objetoAggregateRoot)
      case other =>
        context.actorOf(objetoActorProps, other toString)
    }
  )

  override def setupHandlers(): Unit = {
    commandBus.subscribe[SujetoCommands.SujetoUpdateFromAnt](new SujetoUpdateFromAntHandler(this).handle)
    commandBus.subscribe[SujetoCommands.SujetoUpdateFromTri](new SujetoUpdateFromTriHandler(this).handle)
    commandBus.subscribe[SujetoCommands.SujetoUpdateFromObjeto](new SujetoUpdateFromObjetoHandler(this).handle)
    commandBus.subscribe[SujetoCommands.SujetoSetBajaFromObjeto](new SujetoSetBajaFromObjetoHandler(this).handle)
    queryBus.subscribe[SujetoQueries.GetStateSujeto](new GetStateSujetoHandler(this).handle)
  }

  override def receiveCommand: Receive = customReceiveCommand orElse super.receiveCommand
  def customReceiveCommand: Receive = {
    case childMessage: ObligacionMessage =>
      objetos((childMessage.sujetoId, childMessage.objetoId, childMessage.tipoObjeto)) forward childMessage
    case childMessage: ObjetoMessage =>
      objetos((childMessage.sujetoId, childMessage.objetoId, childMessage.tipoObjeto)) forward childMessage
  }

  override def receiveRecover: Receive = customReceiveRecover orElse super.receiveRecover
  def customReceiveRecover: Receive = {
    case evt: SujetoEvents.SujetoUpdatedFromObjeto =>
      state += evt
      objetos((evt.sujetoId, evt.objetoId, evt.tipoObjeto))
  }

  def persistSnapshot()(handler: () => Unit = () => ()): Unit = {
    val sujetoId = SujetoMessageRoots.extractor(persistenceId).sujetoId
    val event = SujetoSnapshotPersisted(0, sujetoId, state.registro, state.saldo)
    persistEvent(event, SujetoTags.SujetoReadside)(handler)
  }

}

object SujetoActor extends ShardedEntityNoRequirements {
  def props(noRequirements: NoRequirements = NoRequirements()): Props = Props(new SujetoActor)

  object SujetoTags {
    val SujetoReadside: Set[String] = Set("Sujeto")
  }

  type ObjetoAggregateRoot = (String, String, String)
  class SujetoActorRefMap(newActor: (ObjetoAggregateRoot) => ActorRef)
      extends ActorRefMap[ObjetoAggregateRoot](newActor)

}
