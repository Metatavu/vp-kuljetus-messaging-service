package fi.metatavu.vp.messaging.events

import fi.metatavu.vp.messaging.GlobalEventType
import io.quarkus.runtime.annotations.RegisterForReflection
import java.util.*

/**
 * Event for driver working state change
 */
@RegisterForReflection
class DriverWorkingStateChangeGlobalEvent(driverId: UUID, workingStateNew: String) :
    TelematicsDataGlobalEvent(GlobalEventType.DRIVER_WORKING_STATE_CHANGE, driverId) {
}