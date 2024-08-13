package fi.metatavu.vp.messaging.events

import fi.metatavu.vp.messaging.GlobalEventType
import io.quarkus.runtime.annotations.RegisterForReflection
import java.util.*

@RegisterForReflection
data class DriverWorkingStateChangeGlobalEvent(
    override val driverId: UUID,
    val workTypeId: UUID,
    val workingStateNew: WorkingState
) : TelematicsDataGlobalEvent(GlobalEventType.DRIVER_WORKING_STATE_CHANGE, driverId)

