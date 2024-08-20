package fi.metatavu.vp.messaging.events.abstracts

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import fi.metatavu.vp.messaging.events.GlobalEventType
import fi.metatavu.vp.messaging.events.DriverWorkingStateChangeGlobalEvent
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = TelematicsDataGlobalEvent::class, name = "TELEMATICS_DATA"),
    JsonSubTypes.Type(value = DriverWorkingStateChangeGlobalEvent::class, name = "DRIVER_WORKING_STATE_CHANGE")
)
abstract class GlobalEvent(val type: GlobalEventType)