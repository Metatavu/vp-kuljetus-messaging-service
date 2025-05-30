package fi.metatavu.vp.messaging.events

import fi.metatavu.vp.messaging.events.abstracts.GlobalEvent
import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.OffsetDateTime
import java.util.*

@RegisterForReflection
data class TaskGlobalEvent(
    val userId: UUID,
    val taskType: String,
    val taskStatus: String,
    val truckId: UUID,
    val eventTime: OffsetDateTime
    ): GlobalEvent(GlobalEventType.TASK)