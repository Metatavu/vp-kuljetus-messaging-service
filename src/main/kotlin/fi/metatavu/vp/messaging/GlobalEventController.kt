 package fi.metatavu.vp.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.vp.messaging.events.DriverWorkingStateChangeGlobalEvent
import fi.metatavu.vp.messaging.events.GlobalEvent
import io.quarkus.smallrye.reactivemessaging.sendSuspending
import io.smallrye.mutiny.Uni
import io.vertx.core.json.JsonObject
import io.vertx.mutiny.core.eventbus.EventBus
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Event
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.reactive.messaging.Message
import org.jboss.logging.Logger
import java.util.*

 /**
 * Global event controller
 */
@ApplicationScoped
class GlobalEventController {

    @Channel("vp-out")
    var vpEventsEmitter: Emitter<GlobalEvent>? = null

    @ConfigProperty(name = "vp.senderid")
    lateinit var senderId: String

    @Inject
    lateinit var bus: EventBus

    @Inject
    lateinit var objectMapper: ObjectMapper

    @Inject
    lateinit var logger: Logger

    suspend fun publish(event: GlobalEvent) {
        event.senderId = senderId
        vpEventsEmitter?.sendSuspending(event)
        logger.debug("Event sent: ${event.type} from instance $senderId")
    }

    @Incoming("vp-in")
    fun listen(event: Message<JsonObject>): Uni<Void>? {
        val eventType = event.payload.getString("type")
        val payload = when (eventType) {
            GlobalEventType.DRIVER_WORKING_STATE_CHANGE.name -> objectMapper.readValue(
                event.payload.toString(),
                DriverWorkingStateChangeGlobalEvent::class.java
            )
            else -> null
        }
        if (payload == null) {
            logger.error("Failed ro parse the payload $eventType")
            event.nack(Throwable("Failed to parse the payload"))
        }
        if (payload?.senderId != senderId) {
            bus.publish(eventType, payload)
        }

        return Uni.createFrom().voidItem()
    }

}