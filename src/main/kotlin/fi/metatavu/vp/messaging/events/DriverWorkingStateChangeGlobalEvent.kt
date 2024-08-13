package fi.metatavu.vp.messaging.events

import fi.metatavu.vp.messaging.GlobalEventType
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Event for driver working state change
 */
@RegisterForReflection
class DriverWorkingStateChangeGlobalEvent : GlobalEvent(GlobalEventType.DRIVER_WORKING_STATE_CHANGE) {
    var workingStateOld: String? = null
    var workingStateNew: String? = null
}