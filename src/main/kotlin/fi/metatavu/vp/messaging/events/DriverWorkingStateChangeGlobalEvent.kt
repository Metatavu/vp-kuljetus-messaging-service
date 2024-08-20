package fi.metatavu.vp.messaging.events

import fi.metatavu.vp.messaging.events.abstracts.TelematicsDataGlobalEvent
import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.OffsetDateTime
import java.util.*

@RegisterForReflection
data class DriverWorkingStateChangeGlobalEvent(
    override val driverId: UUID,
    val workTypeId: UUID,
    val workingStateNew: WorkingState,
    val time: OffsetDateTime
): TelematicsDataGlobalEvent(GlobalEventType.DRIVER_WORKING_STATE_CHANGE, driverId)

