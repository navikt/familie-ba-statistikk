package no.nav.familie.ba.statistikk

import no.nav.familie.ba.statistikk.domene.SaksstatistikkDvhRepository
import no.nav.familie.eksterne.kontrakter.saksstatistikk.BehandlingDVH
import no.nav.familie.kontrakter.felles.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SaksstatistikkBehandlingConsumer(private val saksstatistikkDvhRepository: SaksstatistikkDvhRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @KafkaListener(topics = ["aapen-barnetrygd-saksstatistikk-behandling-v1"],
                   id = "familie-ba-saksstatistikk-behandling",
                   idIsGroup = false,
                   containerFactory = "vedtakDvhListenerContainerFactory")
    @Transactional
    fun consume(cr: ConsumerRecord<String, String>, ack: Acknowledgment) {
        try {
            logger.info("[${BEHANDLING}] Melding mottatt. offset=${cr.offset()}, key=${cr.key()}")
            secureLogger.info("[${BEHANDLING}] Melding mottatt. offset=${cr.offset()}, key=${cr.key()}, melding=${cr.value()}")

            val json = cr.value()

            //valider at meldingen lar seg deserialisere
            val behandlingDVH = objectMapper.readValue(json, BehandlingDVH::class.java)
            saksstatistikkDvhRepository.lagre(BEHANDLING, cr.offset(), json, behandlingDVH.funksjonellId).apply {
                when {
                    this == 1 -> secureLogger.info("Saksstatistikk-behandling mottatt og lagret: $json")
                    this > 1 -> logger.error("Saksstatistikk-behandling mottatt på nytt. Lagret, merket som duplikat. offset=${cr.offset()} key=${cr.key()}")
                    else -> throw error("Lagring av nytt Saksstatistikk-behandling mislyktes! offset=${cr.offset()} key=${cr.key()}")
                }
            }
            validerBehandlingDvhMotJsonSchema(json)


        } catch (up: Exception) {
            handleException(up, cr, logger, BEHANDLING)
        }
        ack.acknowledge()
    }

    companion object {
        private const val BEHANDLING = "BEHANDLING"
    }
}