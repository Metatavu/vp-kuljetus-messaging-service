package fi.metatavu.vp.messaging.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import fi.metatavu.vp.messaging.events.abstracts.GlobalEvent
import kotlin.reflect.KClass

/**
 * RabbitMQ message consumer
 *
 * @param channel RabbitMQ channel
 * @param typeClass type class of the incoming messages
 */
class MessageConsumer<T: GlobalEvent>(
    channel: Channel,
    private val typeClass: KClass<T>
): DefaultConsumer(channel) {

    private val incomingMessages: MutableMap<String, List<T>> = HashMap()

    private val objectMapper: ObjectMapper
        get() = jacksonObjectMapper().registerModule(JavaTimeModule())


    fun getIncomingMessages(): Map<String, List<T>> {
        return incomingMessages
    }

    fun getIncomingMessages(routingKey: String): List<T> {
        return incomingMessages[routingKey] ?: emptyList()
    }

    fun clearMessages(routingKey: String) {
        incomingMessages.remove(routingKey)
    }

    private fun addIncomingMessage(routingKey: String, message: T) {
        val messages = incomingMessages[routingKey] ?: emptyList()
        incomingMessages[routingKey] = messages + message
    }

    override fun handleDelivery(
        consumerTag: String?,
        envelope: Envelope?,
        properties: AMQP.BasicProperties?,
        body: ByteArray?
    ) {
        if (envelope?.routingKey == null) {
            throw IllegalArgumentException("Routing key is missing")
        }
        val message = objectMapper.readValue(body, typeClass.java)
        addIncomingMessage(envelope.routingKey, message)
        super.handleDelivery(consumerTag, envelope, properties, body)
    }
}