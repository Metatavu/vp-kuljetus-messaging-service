package fi.metatavu.vp.messaging.events

import fi.metatavu.vp.messaging.GlobalEventType
import io.quarkus.runtime.annotations.RegisterForReflection
import java.util.UUID

/**
 * Abstract class for global events
 */
@RegisterForReflection
abstract class GlobalEvent(val type: GlobalEventType) {
    var senderId: String = ""
    var payload: Any? = null
}