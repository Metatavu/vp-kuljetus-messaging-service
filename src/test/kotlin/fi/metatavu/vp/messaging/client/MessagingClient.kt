package fi.metatavu.vp.messaging.client

import fi.metatavu.vp.messaging.RoutingKey
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import fi.metatavu.vp.usermanagement.settings.RabbitMQTestProfile.Companion.EXCHANGE_NAME
import io.vertx.core.json.JsonObject
import org.eclipse.microprofile.config.ConfigProvider

/**
 * Singleton class for RabbitMQ messaging client for testing purposes
 */
object MessagingClient {

    private var connection: Connection? = null
    private var channel: Channel? = null

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
            JsonObject.mapFrom(message).encode().toByteArray()
        )
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
    }
}