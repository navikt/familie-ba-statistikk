package no.nav.familie.ba.statistikk.integrasjoner

import no.nav.familie.ba.statistikk.SaksstatistikkBehandlingConsumer
import no.nav.familie.ba.statistikk.SaksstatistikkSakConsumer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaProducer {

    private val logger = LoggerFactory.getLogger(KafkaProducer::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @Autowired
    lateinit var kafkaOnpremProducer: KafkaTemplate<String, String>

    fun sendSakstatistikkSakmelding(funksjonellId: String, melding: String): Long {
        val record = kafkaOnpremProducer.send(SaksstatistikkSakConsumer.TOPIC_NAVN, funksjonellId, melding).get()
        secureLogger.info("Sendt sendSakstatistikkSakmelding: ${record.recordMetadata.offset()} $funksjonellId-$melding")
        return record.recordMetadata.offset()
    }

    fun sendSakstatistikkBehandlingmelding(funksjonellId: String, melding: String): Long {
        val record = kafkaOnpremProducer.send(SaksstatistikkBehandlingConsumer.TOPIC_NAVN, funksjonellId, melding).get()
        secureLogger.info("Sendt sendSakstatistikkBehandlingmelding: ${record.recordMetadata.offset()} $funksjonellId-$melding")
        return record.recordMetadata.offset()
    }
}
