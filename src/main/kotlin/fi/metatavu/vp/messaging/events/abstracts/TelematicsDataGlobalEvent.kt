package fi.metatavu.vp.messaging.events.abstracts

import fi.metatavu.vp.messaging.events.GlobalEventType
import io.quarkus.runtime.annotations.RegisterForReflection
import java.util.*

@RegisterForReflection
abstract class TelematicsDataGlobalEvent(
    type: GlobalEventType,
    open val driverId: UUID
): GlobalEvent(type)