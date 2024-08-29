package fi.metatavu.vp.messaging.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rabbitmq.client.*
import fi.metatavu.vp.messaging.RoutingKey
import fi.metatavu.vp.messaging.events.abstracts.GlobalEvent
import org.eclipse.microprofile.config.ConfigProvider

/**
 * Singleton class for RabbitMQ messaging client for testing purposes
 */
object MessagingClient {

    private var connection: Connection? = null
    private var channel: Channel? = null

    private val incomingMessages: MutableMap<String, List<GlobalEvent>> = HashMap()

    private const val EXCHANGE_NAME = "test-exchange"

    init {
        setupClient()
    }

    private val port: Int
        get() = ConfigProvider.getConfig().getValue("rabbitmq-port", Int::class.java)
    private val username: String
        get() = ConfigProvider.getConfig().getValue("rabbitmq-username", String::class.java)
    private val password: String
        get() = ConfigProvider.getConfig().getValue("rabbitmq-password", String::class.java)
    private val hostName: String
        get() = ConfigProvider.getConfig().getValue("rabbitmq-host", String::class.java)

    private val objectMapper: ObjectMapper
        get() = jacksonObjectMapper().registerModule(JavaTimeModule())

    /**
     * Publishes a message to the RabbitMQ exchange
     *
     * @param message message to be published
     */
    fun publishMessage(message: Any) {
        val props = AMQP.BasicProperties()
            .builder()
            .contentType("application/json")
            .build()
        channel?.basicPublish(
            EXCHANGE_NAME,
            RoutingKey.DRIVER_WORKING_STATE_CHANGE.name,
            props,
            objectMapper.writeValueAsBytes(message)
        )
    }

    fun getIncomingMessages(): Map<String, List<GlobalEvent>> {
        return incomingMessages
    }

    fun <T: GlobalEvent> getIncomingMessages(routingKey: String): List<T> {
        return incomingMessages[routingKey] as List<T>? ?: emptyList()
    }

    fun clearMessages(routingKey: String) {
        incomingMessages.remove(routingKey)
    }

    private fun addIncomingMessage(routingKey: String, message: GlobalEvent) {
        val messages = incomingMessages[routingKey] ?: emptyList()
        incomingMessages[routingKey] = messages + message
    }

    /**
     * Sets up the RabbitMQ client.
     *
     * e.g. connects to the broker and declares the exchange
     */
    private fun setupClient() {
        val factory = ConnectionFactory()
        factory.username = username
        factory.password = password
        factory.host = hostName
        factory.port = port
        val newConnection = factory.newConnection()
        val newChannel = newConnection.createChannel()

        connection = newConnection
        channel = newChannel

        channel?.exchangeDeclare(EXCHANGE_NAME, "topic", true)
        val queueName = channel?.queueDeclare()?.queue
        channel?.queueBind(queueName, EXCHANGE_NAME, RoutingKey.DRIVER_WORKING_STATE_CHANGE.name)

        channel?.basicConsume(
            queueName,
            object: DefaultConsumer(channel) {
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
            },
        )
    }
}