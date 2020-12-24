import io.ktor.application.install
import io.ktor.features.AutoHeadResponse
import io.ktor.http.content.default
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.http.content.staticRootFolder
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.io.File

fun main() {
    embeddedServer(Netty, 8080) {
        install(AutoHeadResponse)

        routing {
            static("/") {
                staticRootFolder = File(System.getenv("STATIC_FILES_DIR") ?: ".").absoluteFile
                default("index.html")

                static("css") {
                    files("css")
                }

                static("js") {
                    files("js")
                }
            }
        }
    }.start(wait = true)
}