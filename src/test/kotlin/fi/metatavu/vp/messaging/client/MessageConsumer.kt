package fi.metatavu.vp.messaging.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import fi.metatavu.vp.messaging.events.abstracts.GlobalEvent
import org.testcontainers.shaded.org.awaitility.Awaitility
import java.util.concurrent.TimeUnit

/**
 * RabbitMQ message consumer
 *
 * @param channel RabbitMQ channel
 */
class MessageConsumer(channel: Channel): DefaultConsumer(channel) {

    private val incomingMessages: MutableMap<String, MutableList<GlobalEvent>> = HashMap()

    private val objectMapper: ObjectMapper
        get() = jacksonObjectMapper().registerModule(JavaTimeModule())

    override fun handleDelivery(
        consumerTag: String?,
        envelope: Envelope?,
        properties: AMQP.BasicProperties?,
        body: ByteArray?
    ) {
        if (envelope?.routingKey == null) {
            throw IllegalArgumentException("Routing key is missing")
        }
        val message = objectMapper.readValue(body, GlobalEvent::class.java)
        addIncomingMessage(envelope.routingKey, message)
        super.handleDelivery(consumerTag, envelope, properties, body)
    }

    /**
     * Waits for x-amount of incoming messages for a specific routing key
     *
     * Clears the messages after.
     *
     * @param routingKey routing key
     * @param waitCount number of messages to wait
     * @param waitTime time to wait for messages in minutes
     * @return list of messages
     */
    @Suppress("UNCHECKED_CAST")
    fun <T: GlobalEvent> consumeMessages(routingKey: String, waitCount: Int, waitTime: Long = 1): List<T> {
        Awaitility
            .await()
            .atMost(waitTime, TimeUnit.MINUTES)
            .until { (incomingMessages[routingKey]?.size ?: 0) >= waitCount }

        return clearMessages(routingKey) as List<T>? ?: emptyList()
    }

    /**
     * Clears incoming messages with given routing key
     *
     * @param routingKey routing key
     * @return list of messages associated with the cleared key
     */
    private fun clearMessages(routingKey: String): MutableList<GlobalEvent>? {
        return incomingMessages.remove(routingKey)
    }

    /**
     * Adds incoming message to the list of incoming messages
     *
     * @param routingKey routing key
     * @param message message
     */
    private fun addIncomingMessage(routingKey: String, message: GlobalEvent) {
        val messages = incomingMessages[routingKey] ?: emptyList()
        incomingMessages[routingKey] = (messages + message).toMutableList()
    }
}