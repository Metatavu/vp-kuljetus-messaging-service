package fi.metatavu.vp.messaging.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rabbitmq.client.*
import fi.metatavu.vp.messaging.RoutingKey
import org.eclipse.microprofile.config.ConfigProvider

/**
 * Singleton class for RabbitMQ messaging client for testing purposes
 */
object MessagingClient {

    private lateinit var connection: Connection
    private lateinit var channel: Channel
    private lateinit var queueName: String

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
        channel.basicPublish(
            EXCHANGE_NAME,
            RoutingKey.DRIVER_WORKING_STATE_CHANGE.name,
            props,
            objectMapper.writeValueAsBytes(message)
        )
    }

    /**
     * Sets a consumer for the RabbitMQ client
     *
     * @return message consumer
     */
    fun setConsumer(): MessageConsumer {
        val consumer = MessageConsumer(channel)
        channel.basicConsume(
            queueName,
            consumer,
        )

        return consumer
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

        connection = factory.newConnection()
        channel = connection.createChannel()

        channel.exchangeDeclare(EXCHANGE_NAME, "topic", true)
        val queueName = channel.queueDeclare().queue
        channel.queueBind(queueName, EXCHANGE_NAME, RoutingKey.DRIVER_WORKING_STATE_CHANGE.name)
    }
}