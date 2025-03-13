package fi.metatavu.vp.messaging.events

import fi.metatavu.vp.messaging.events.abstracts.GlobalEvent
import io.quarkus.runtime.annotations.RegisterForReflection
import java.util.*

@RegisterForReflection
data class TemperatureGlobalEvent(
    val thermometerId: UUID,
    val temperature: Float,
    val timestamp: Long
): GlobalEvent(GlobalEventType.TEMPERATURE)