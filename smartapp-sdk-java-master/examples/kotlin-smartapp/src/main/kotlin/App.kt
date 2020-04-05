package app

import app.routes.smartAppExecution
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.JacksonConverter
import io.ktor.jackson.jackson
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import org.slf4j.event.Level

fun Application.main() {

    /**
     * This automatically adds Date and Server headers to each response, and would allow you to configure
     * additional headers served to each response.
     * @see <a href="https://ktor.io/servers/features/default-headers.html">Default Headers</a>
     */
    install(DefaultHeaders)

    /**
     * Log client requests - the basic un-configured feature logs every request using the level TRACE
     * @see <a href="https://ktor.io/servers/features/call-logging.html">Call Logging</a>
     */
    install(CallLogging) {
        level = Level.INFO

        // Only log requests that match POST /smartapp
        filter { call ->
            call.request.path().startsWith("/smartapp")
                && call.request.httpMethod == HttpMethod.Post
        }
    }

    /**
     * Automatic de-/serialization based on Content-Type
     * @see <a href="https://ktor.io/servers/features/content-negotiation.html">Content Negotiation</a>
     */
    install(ContentNegotiation) {
        jackson {
            // Don't fail in case the existing model is missing added properties
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            // Exclude null value fields for brevity
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
        register(ContentType.Application.Json, JacksonConverter())
    }

    /**
     * Adds structured page request handling
     * @see <a href="https://ktor.io/servers/features/routing.html">Routing</a>
     */
    install(Routing) {
        // Handle a root-level GET request - this is used by unit tests
        get("/") {
            call.respond(HttpStatusCode.Forbidden, "This app only functions as a SmartThings Automation webhook endpoint app")
        }
        // Handle POST /smartapp
        // This is a critical path to the "smartapp-core" functionality
        route("/smartapp") {
            smartAppExecution()
        }
    }
}
