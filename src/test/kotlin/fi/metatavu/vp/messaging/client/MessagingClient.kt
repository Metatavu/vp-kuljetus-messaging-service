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

    private lateinit var connection: Connection
    lateinit var channel: Channel
    val queues: MutableMap<String, MessageConsumer<*>> = HashMap()

    const val EXCHANGE_NAME = "test-exchange"

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
    fun publishMessage(message: Any, routingKey: RoutingKey) {
        val props = AMQP.BasicProperties()
            .builder()
            .contentType("application/json")
            .build()
        channel.basicPublish(
            EXCHANGE_NAME,
            routingKey.name,
            props,
            objectMapper.writeValueAsBytes(message)
        )
    }

    /**
     * Sets a consumer for the RabbitMQ client
     *
     * @param routingKey routing key
     * @return message consumer
     */
    inline fun <reified T: GlobalEvent> setConsumer(routingKey: RoutingKey): MessageConsumer<T> {
        val queueName = channel.queueDeclare().queue
        channel.queueBind(queueName, EXCHANGE_NAME, routingKey.name)
        val consumer = MessageConsumer(channel, routingKey.name, T::class)
        channel.basicConsume(
            queueName,
            consumer,
        )

        queues[queueName] = consumer

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
    }
}