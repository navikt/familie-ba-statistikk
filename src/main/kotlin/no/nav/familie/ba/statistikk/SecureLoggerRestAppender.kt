package no.nav.familie.ba.statistikk

import ch.qos.logback.core.AppenderBase
import ch.qos.logback.core.Layout
import no.nav.familie.kontrakter.felles.objectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class SecureLoggerRestAppender : AppenderBase<ch.qos.logback.classic.spi.ILoggingEvent>() {

    lateinit var layout: Layout<ch.qos.logback.classic.spi.ILoggingEvent>

    val client = HttpClient.newHttpClient()

    override fun append(eventObject: ch.qos.logback.classic.spi.ILoggingEvent) {
        println("Skriver til sikkerlogg")

        var logEvent = mutableMapOf<String, String>()
        logEvent["message"] = eventObject.message
        logEvent["level"] = eventObject.level.levelStr
        logEvent["thread"] = eventObject.threadName
        val t = eventObject.throwableProxy
        if (t != null) {
            val ste = t.stackTraceElementProxyArray

            var sb = StringBuffer()

            for (i in ste) {
                sb.appendLine(i.steAsString)
            }



            logEvent["stack_trace"] = sb.toString()
            logEvent["exception"] = t.className
        }

        val mdc = eventObject.mdcPropertyMap
        mdc.forEach { k, v ->
            logEvent[k] = v
        }

        val request = HttpRequest.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .uri(URI.create("http://localhost:19880/"))
            .timeout(Duration.ofSeconds(10))
            .setHeader("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(logEvent)))
            .build()

        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        println("Response fra POST sikkerlog: ${response.statusCode()} ${response.body()} ${response.headers()}")

    }
}