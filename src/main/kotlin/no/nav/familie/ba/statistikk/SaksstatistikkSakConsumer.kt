package no.nav.familie.ba.statistikk

import no.nav.familie.ba.statistikk.domene.SaksstatistikkDvhRepository
import no.nav.familie.eksterne.kontrakter.saksstatistikk.SakDVH
import no.nav.familie.kontrakter.felles.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SaksstatistikkSakConsumer(private val saksstatistikkDvhRepository: SaksstatistikkDvhRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @KafkaListener(topics = ["aapen-barnetrygd-saksstatistikk-sak-v1"],
                   id = "familie-ba-saksstatistikk-sak",
                   idIsGroup = false,
                   containerFactory = "vedtakDvhListenerContainerFactory")
    @Transactional
    fun consume(cr: ConsumerRecord<String, String>, ack: Acknowledgment) {
        try {
            logger.info("[${SAK}] Melding mottatt. offset=${cr.offset()}, key=${cr.key()}")
            secureLogger.info("[${SAK}] Melding mottatt. offset=${cr.offset()}, key=${cr.key()}, melding=${cr.value()}")

            val json = cr.value()

            //valider at meldingen lar seg deserialisere
            val sakDVH = objectMapper.readValue(json, SakDVH::class.java)
            saksstatistikkDvhRepository.lagre(SAK, cr.offset(), json, sakDVH.funksjonellId).apply {
                when {
                    this == 1 -> secureLogger.info("Saksstatistikk-sak mottatt og lagret: $json")
                    this > 1 -> logger.error("Saksstatistikk-sak mottatt på nytt. Lagret, merket som duplikat. offset=${cr.offset()} key=${cr.key()}")
                    else -> throw error("Lagring av nytt Saksstatistikk-sak mislyktes! offset=${cr.offset()} key=${cr.key()}")
                }
            }

            validerSakDvhMotJsonSchema(json)
        } catch (up: Exception) {
            handleException(up, cr, logger, SAK)
        }
        ack.acknowledge()
    }

    companion object {
        private const val SAK = "SAK"
    }

}