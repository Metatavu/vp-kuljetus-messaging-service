package fi.metatavu.vp.messaging.events

import fi.metatavu.vp.messaging.GlobalEventType
import io.quarkus.runtime.annotations.RegisterForReflection
import java.util.*

/**
 * Abstract base class for all global events related to telematics data
 */
@RegisterForReflection
abstract class TelematicsDataGlobalEvent(type: GlobalEventType, driverId: UUID) : GlobalEvent(type = type) {
}