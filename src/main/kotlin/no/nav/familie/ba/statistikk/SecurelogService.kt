package no.nav.familie.ba.statistikk

import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.lang.RuntimeException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration


@Service
class SecurelogService() {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @Scheduled(fixedDelay = 60000)
    fun secureLog() {
        secureLogger.info("Sikkerlog uten mdc")
        MDC.put("callId", "test")
        MDC.put("foo", "bar")
        logger.info("Testing secure log")
        secureLogger.info("Sikkerlog med mdc")

        try {
            kastException()
        } catch (e: Exception) {
            secureLogger.info("Sikkerlog med throwable", e)
        }

        MDC.clear()
    }

    private fun kastException() {
        throw RuntimeException("Feil")
    }
}