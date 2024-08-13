 package fi.metatavu.vp.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.vp.messaging.events.DriverWorkingStateChangeGlobalEvent
import fi.metatavu.vp.messaging.events.GlobalEvent
import io.smallrye.mutiny.Uni
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata
import io.vertx.core.json.JsonObject
import io.vertx.mutiny.core.eventbus.EventBus
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.reactive.messaging.*
import org.jboss.logging.Logger

 /**
 * Global event controller
 */
@ApplicationScoped
class GlobalEventController {

    @Channel("vp-out")
    var vpEventsEmitter: Emitter<GlobalEvent>? = null

    @Inject
    lateinit var bus: EventBus

    @Inject
    lateinit var objectMapper: ObjectMapper

    @Inject
    lateinit var logger: Logger

     /**
      * Publishes global event
      *
      * @param event event to publish
      */
     fun publish(event: GlobalEvent) {
         val message = Message.of(
             event, Metadata.of(
                 OutgoingRabbitMQMetadata.Builder()
                     .withRoutingKey(event.type.name)
                     .build()
             )
         )
         vpEventsEmitter?.send(message)
    }

    /**
     * Listens to incoming global events
     *
     * @param event incoming event
     * @return
     */
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

        bus.publish(eventType, payload)

        return Uni.createFrom().voidItem()
    }

}