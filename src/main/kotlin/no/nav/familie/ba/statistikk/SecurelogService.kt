package no.nav.familie.ba.statistikk

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*


@Service
@Deprecated("Midlertidig service for test")
class SecurelogService {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")


    @Scheduled(fixedDelay = 60000*60)
    fun secureLog() {
        try {
            MDC.put("callId", UUID.randomUUID().toString())
            kastException()
        } catch (e: Exception) {
            logger.info("Tester securelogs med rest", e)
            secureLogger.info("Tester securelogs med rest", e)
        } finally {
            MDC.clear()
        }
    }

    private fun kastException() {
        throw RuntimeException("Test av feil")
    }
}