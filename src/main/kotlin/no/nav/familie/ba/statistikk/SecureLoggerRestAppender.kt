package no.nav.familie.ba.statistikk

import ch.qos.logback.core.AppenderBase
import ch.qos.logback.core.Layout
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class SecureLoggerRestAppender<ILoggingEvent> : AppenderBase<ch.qos.logback.classic.spi.ILoggingEvent>() {

    lateinit var layout: Layout<ch.qos.logback.classic.spi.ILoggingEvent>

    val client = HttpClient.newHttpClient()

    override fun append(eventObject: ch.qos.logback.classic.spi.ILoggingEvent) {
        val message = layout.doLayout(eventObject)


        val request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:19880"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .build()

        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

    }
}