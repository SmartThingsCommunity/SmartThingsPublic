import app.main
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import kotlin.test.assertEquals

class AppTest {

    /**
     * Tests the index page
     */
    @Test
    fun testGetIndex() = testApp {
        handleRequest(HttpMethod.Get, "/").apply {
            assertEquals(403, response.status()?.value)
        }
    }

    /**
     * Convenience method we use to configure a test application and to execute a [callback] block testing it.
     */
    private fun testApp(callback: TestApplicationEngine.() -> Unit): Unit {
        withTestApplication({
            main()
        }, callback)
    }
}
