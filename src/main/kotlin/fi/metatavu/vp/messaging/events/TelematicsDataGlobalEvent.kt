package fi.metatavu.vp.messaging.events

import fi.metatavu.vp.messaging.GlobalEventType
import io.quarkus.runtime.annotations.RegisterForReflection
import java.util.*

@RegisterForReflection
abstract class TelematicsDataGlobalEvent(type: GlobalEventType, open val driverId: UUID) : GlobalEvent(type)