package consumers.registral.etapas_procesales.domain

import java.time.LocalDateTime

import consumers.registral.etapas_procesales.application.entities.{EtapasProcesalesExternalDto, EtapasProcesalesMessage}
import cqrs.BasePersistentShardedTypedActor.CQRS.AbstractStateWithCQRS

case class EtapasProcesalesState(
    registro: Option[EtapasProcesalesExternalDto] = None,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN
) extends AbstractStateWithCQRS[EtapasProcesalesMessage, EtapasProcesalesEvents, EtapasProcesalesState]
