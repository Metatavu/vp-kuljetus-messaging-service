package fi.metatavu.vp.messaging.events

import fi.metatavu.vp.messaging.GlobalEventType
import java.util.*

/**
 * Abstract base class for all global events related to telematics data
 */
abstract class TelematicsDataGlobalEvent(type: GlobalEventType, driverId: UUID) : GlobalEvent(type = type) {
}