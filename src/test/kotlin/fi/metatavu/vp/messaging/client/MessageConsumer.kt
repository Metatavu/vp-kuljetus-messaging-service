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
import kotlin.reflect.KClass

/**
 * RabbitMQ message consumer
 *
 * @param channel RabbitMQ channel
 * @param routingKey routing key
 * @param typeClass type class of messages to be handled by the consumer
 */
class MessageConsumer<T: GlobalEvent>(
    channel: Channel,
    private val routingKey: String,
    private val typeClass: KClass<T>
): DefaultConsumer(channel) {

    private val messages = mutableListOf<T>()

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
        if (envelope.routingKey != routingKey) {
            println("Routing key does not match: ${envelope.routingKey}")
            return
        }
        val message = objectMapper.readValue(body, typeClass.java)
        messages.add(message)
        super.handleDelivery(consumerTag, envelope, properties, body)
    }

    /**
     * Waits for x-amount of incoming messages
     *
     * Clears the messages after.
     *
     * @param waitCount number of messages to wait
     * @param waitTime time to wait for messages in minutes
     * @return list of messages
     */
    fun consumeMessages(waitCount: Int, waitTime: Long = 1): List<T> {
        val arrivedMessages = mutableListOf<T>()
        Awaitility
            .await()
            .atMost(waitTime, TimeUnit.MINUTES)
            .until { messages.size >= waitCount }
        arrivedMessages.addAll(messages)
        messages.clear()

        return arrivedMessages
    }
}