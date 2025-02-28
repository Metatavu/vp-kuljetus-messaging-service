package fi.metatavu.vp.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.metatavu.vp.messaging.events.DriverWorkEventGlobalEvent
import fi.metatavu.vp.messaging.events.GlobalEventType
import fi.metatavu.vp.messaging.events.TemperatureGlobalEvent
import fi.metatavu.vp.messaging.events.abstracts.GlobalEvent
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
@Suppress("unused")
class GlobalEventController {

    @Channel("vp-out")
    var eventsEmitter: Emitter<GlobalEvent>? = null

    @Inject
    lateinit var eventBus: EventBus

    @Inject
    lateinit var logger: Logger

    private val objectMapper: ObjectMapper
        get() = jacksonObjectMapper().registerModule(JavaTimeModule())

     /**
      * Publishes global event
      *
      * @param event event to publish
      */
     fun publish(event: GlobalEvent) {
         val messageMetadata = Metadata.of(
             OutgoingRabbitMQMetadata.Builder()
                 .withRoutingKey(event.type.name)
                 .build()
         )
         val message = Message.of(event, messageMetadata)
         eventsEmitter?.send(message)
    }

    /**
     * Listens to incoming global events
     *
     * @param event incoming event
     */
    @Incoming("vp-in")
    fun listen(event: Message<JsonObject>): Uni<Void>? {
        val (eventType, payload) = deserializeEvent(event.payload)

        if (payload == null) {
            logger.error("Failed to parse the payload $eventType")
            event.nack(Throwable("Failed to parse the payload"))
        }

        logger.debug("Parsed $eventType event\n$payload")

        eventBus.publish(eventType, payload)

        return Uni.createFrom().voidItem()
    }

     /**
      * Deserializes global event
      *
      * @param event event to deserialize
      * @return Pair of events type and deserialized event
      */
     private fun deserializeEvent(event: JsonObject): Pair<String, GlobalEvent?> {
         val eventType = event.getString("type")

         val payload = when (eventType) {
             GlobalEventType.DRIVER_WORKING_STATE_CHANGE.name -> objectMapper.readValue(event.toString(), DriverWorkEventGlobalEvent::class.java)
             GlobalEventType.TEMPERATURE.name -> objectMapper.readValue(event.toString(), TemperatureGlobalEvent::class.java)
             else -> null
         }

         return eventType to payload
     }

}