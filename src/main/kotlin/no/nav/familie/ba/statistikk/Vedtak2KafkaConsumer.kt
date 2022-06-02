package no.nav.familie.ba.statistikk

import no.nav.familie.ba.statistikk.domene.VedtakDVHType
import no.nav.familie.ba.statistikk.domene.VedtakDvhRepository
import no.nav.familie.eksterne.kontrakter.VedtakDVHV2
import no.nav.familie.kontrakter.felles.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class Vedtak2KafkaConsumer(private val vedtakDvhRepository: VedtakDvhRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @KafkaListener(topics = ["teamfamilie.aapen-barnetrygd-vedtak-v2"],
                   groupId = "statistikk-v2",
                   id = "familie-ba-statistikk-v2",
                   idIsGroup = false,
                   containerFactory = "kafkaAivenHendelseListenerContainerFactory")
    @Transactional
    fun consume(cr: ConsumerRecord<String, String>, ack: Acknowledgment) {
        try {
            val vedtak = cr.value()

            //valider at meldingen lar seg deserialisere
            val vedtakDVH = objectMapper.readValue(vedtak, VedtakDVHV2::class.java)

            vedtakDvhRepository.lagre(cr.offset(), vedtak, VedtakDVHType.VEDTAK_V2, vedtakDVH.behandlingsId, vedtakDVH.funksjonellId).apply {
                when {
                    this == 1 -> secureLogger.info("VedtakV2 mottatt og lagret: $vedtak")
                    this > 1 -> logger.error("VedtakV2 mottatt på nytt. Lagret, merket som duplikat. offset=${cr.offset()} key=${cr.key()}")
                    else -> throw error("Lagring av nytt vedtakV2 mislyktes! offset=${cr.offset()} key=${cr.key()}")
                }
            }




        } catch (up: Exception) {
            handleException(up, cr, logger, "VEDTAKV2")
        }
        ack.acknowledge()
    }
}
