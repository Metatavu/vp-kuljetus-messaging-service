package fi.metatavu.vp.messaging

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
enum class GlobalEventType {
  DRIVER_WORKING_STATE_CHANGE
}