package fi.metatavu.vp.messaging.events

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
enum class WorkingState {
    WORKING,
    NOT_WORKING
}