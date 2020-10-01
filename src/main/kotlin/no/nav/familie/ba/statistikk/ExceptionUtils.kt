package no.nav.familie.ba.statistikk

import com.fasterxml.jackson.core.JsonProcessingException
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException

private val secureLogger = LoggerFactory.getLogger("secureLogger")

fun handleException(up: Exception,
                    cr: ConsumerRecord<String, String>,
                    logger: Logger,
                    type: String) {
    when (up) {
        is DataIntegrityViolationException -> {
            logger.error("[$type] Fikk melding som ikke var gyldig json, hopper over melding. offset=${cr.offset()} key=${cr.key()}")
            secureLogger.error("[$type] Fikk melding som ikke var gyldig json. offset=${cr.offset()} key=${cr.key()} melding=${cr.value()}",
                               up)

        }
        is JsonProcessingException -> {
            logger.error("[$type] Fikk melding som ikke er i henhold til gjeldende kontrakt. offset=${cr.offset()} key=${cr.key()}")
            secureLogger.error("[$type] Fikk melding som ikke er i henhold til gjeldende kontrakt. offset=${cr.offset()} key=${cr.key()} melding=${cr.value()}",
                               up)
            //Må ta ibruk ny kontrakt for å kunne lese disse meldingene
            throw up
        }
        else -> throw up
    }
}