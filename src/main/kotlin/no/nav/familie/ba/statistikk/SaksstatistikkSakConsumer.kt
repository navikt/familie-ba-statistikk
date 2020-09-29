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
            val json = cr.value()

            saksstatistikkDvhRepository.lagre("SAK", cr.offset(), json)

            secureLogger.info("Saksstatistikk-sak mottatt og lagret: $json")

            //valider at meldingen lar seg deserialisere
            objectMapper.readValue(json, SakDVH::class.java)

        } catch (up: Exception) {
            handleException(up, cr, logger, "SAK")
        }
        ack.acknowledge()
    }


}