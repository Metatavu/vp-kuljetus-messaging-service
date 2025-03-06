package fi.metatavu.vp.messaging.events

import fi.metatavu.vp.messaging.events.abstracts.GlobalEvent
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class TemperatureGlobalEvent(
    val sensorId: String,
    val temperature: Float,
    val timestamp: Long
): GlobalEvent(GlobalEventType.TEMPERATURE)