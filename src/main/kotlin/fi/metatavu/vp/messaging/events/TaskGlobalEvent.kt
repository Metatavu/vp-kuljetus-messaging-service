package fi.metatavu.vp.messaging.events

import fi.metatavu.vp.api.model.TaskStatus
import fi.metatavu.vp.api.model.TaskType
import fi.metatavu.vp.messaging.events.abstracts.GlobalEvent
import io.quarkus.runtime.annotations.RegisterForReflection
import java.util.*

@RegisterForReflection
data class TaskGlobalEvent(
    val userId: UUID,
    val taskType: TaskType,
    val taskStatus: TaskStatus
    ): GlobalEvent(GlobalEventType.TASK)