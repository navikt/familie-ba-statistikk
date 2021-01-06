package no.nav.familie.ba.statistikk

import ch.qos.logback.core.AppenderBase
import no.nav.familie.kontrakter.felles.objectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class SecureLoggerRestAppender : AppenderBase<ch.qos.logback.classic.spi.ILoggingEvent>() {

    val client = HttpClient.newHttpClient()

    override fun append(eventObject: ch.qos.logback.classic.spi.ILoggingEvent) {
        var logEvent = mutableMapOf<String, String>()
        logEvent["message"] = eventObject.message
        logEvent["level"] = eventObject.level.levelStr
        logEvent["thread"] = eventObject.threadName
        val iThrowableProxy = eventObject.throwableProxy


        if (iThrowableProxy != null) {
            var sb = StringBuilder()
            sb.appendLine("${iThrowableProxy.className}: ${iThrowableProxy.message}")

            val ste = iThrowableProxy.stackTraceElementProxyArray
            for (i in ste) {
                sb.appendLine(i.steAsString)
            }

            val stackTrace = "${iThrowableProxy.className}: ${iThrowableProxy.message} ${
                iThrowableProxy.stackTraceElementProxyArray.joinToString { "${it.steAsString}\n" }
            }"

            logEvent["stack_trace"] = stackTrace
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

        if (response.statusCode() != 200) {
            println("ERROR ved posting av melding til secureLog ${response.statusCode()} ${response.body()} ${response.headers()}")
        }
    }
}