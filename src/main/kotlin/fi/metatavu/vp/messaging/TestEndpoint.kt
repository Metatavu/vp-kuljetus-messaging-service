package fi.metatavu.vp.messaging

import fi.metatavu.vp.messaging.events.GlobalEvent
import io.quarkus.arc.profile.IfBuildProfile
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * Test endpoint to trigger an event, should be accessible only for tests
 */
@RequestScoped
@Path("/test-rabbitmq")
@IfBuildProfile("test")
class TestEndpoint {

    @Inject
    lateinit var globalEventController: GlobalEventController

    @ConfigProperty(name = "env")
    lateinit var env: Optional<String>

    @POST
    @Consumes("application/json")
    suspend fun triggerEvent(event: GlobalEvent): jakarta.ws.rs.core.Response {
        if (env.isEmpty || env.getOrNull() != "TEST") {
            return jakarta.ws.rs.core.Response.status(jakarta.ws.rs.core.Response.Status.FORBIDDEN).build()
        }
        globalEventController.publish(event)
        return jakarta.ws.rs.core.Response.ok().build()
    }
}