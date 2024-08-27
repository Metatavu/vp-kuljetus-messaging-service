package fi.metatavu.vp.messaging.events

import fi.metatavu.vp.api.model.WorkEventType
import fi.metatavu.vp.messaging.events.abstracts.TelematicsDataGlobalEvent
import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.OffsetDateTime
import java.util.*

@RegisterForReflection
data class DriverWorkEventGlobalEvent(
    override val driverId: UUID,
    val workEventType: WorkEventType,
    val time: OffsetDateTime
): TelematicsDataGlobalEvent(GlobalEventType.DRIVER_WORKING_STATE_CHANGE, driverId)
