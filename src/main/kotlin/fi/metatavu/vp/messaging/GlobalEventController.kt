package fi.metatavu.vp.messaging

import fi.metatavu.vp.messaging.events.DriverWorkEventGlobalEvent
import fi.metatavu.vp.messaging.events.GlobalEventType
import fi.metatavu.vp.messaging.events.abstracts.GlobalEvent
import fi.metatavu.vp.usermanagement.WithCoroutineScope
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.smallrye.reactive.messaging.rabbitmq.IncomingRabbitMQMessage
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
class GlobalEventController: WithCoroutineScope() {

    @Channel("vp-out")
    var eventsEmitter: Emitter<GlobalEvent>? = null

    @Inject
    lateinit var eventBus: EventBus

    @Inject
    lateinit var logger: Logger

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
    fun listen(event: IncomingRabbitMQMessage<JsonObject>): Uni<Void>? = withCoroutineScope {
        val (eventType, payload) = deserializeEvent(event.payload)

        if (payload == null) {
            logger.error("Failed to parse the payload $eventType")
            event.nack(Throwable("Failed to parse the payload"))
        }

        logger.debug("Parsed $eventType event\n$payload")

        eventBus.request<Boolean>(eventType, payload)
            .onFailure()
            .invoke { throwable -> onFailure(event, throwable) }
            .onItem()
            .transform { it.body() }
            .invoke { success -> onItem(event, success) }
            .awaitSuspending()
    }.replaceWithVoid()

    /**
     * Callback to invoke when event bus message processing fails
     *
     * Nacks the message.
     *
     * @param message incoming message
     * @param throwable throwable that caused the failure
     */
    private fun onFailure(message: IncomingRabbitMQMessage<*>, throwable: Throwable) {
        logger.error("Failed to process the message", message, throwable)
        message.nack(throwable)
    }

    /**
     * Callback to invoke when event bus message processing is successful
     *
     * Acks the message if successful, nacks otherwise.
     *
     * @param message incoming message
     * @param success whether the processing was successful
     */
    private fun onItem(message: IncomingRabbitMQMessage<*>, success: Boolean) {
        if (success) {
            message.ack()
            logger.info("Message processed successfully and acked")
        } else {
            message.nack(Throwable("Failed to process the event"))
            logger.error("Failed to process the message")
        }
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
            GlobalEventType.DRIVER_WORKING_STATE_CHANGE.name -> event.mapTo(DriverWorkEventGlobalEvent::class.java)
            else -> null
        }

        return eventType to payload
    }
}