package no.nav.familie.ba.statistikk

import no.nav.familie.ba.statistikk.domene.VedtakDvhRepository
import no.nav.familie.eksterne.kontrakter.VedtakDVH
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VedtakKafkaConsumer(private val vedtakDvhRepository: VedtakDvhRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @KafkaListener(topics = ["aapen-barnetrygd-vedtak-v1"],
                   id = "familie-ba-statistikk",
                   idIsGroup = false,
                   containerFactory = "vedtakDvhListenerContainerFactory")
    @Transactional
    fun consume(vedtak: VedtakDVH, ack: Acknowledgment) {
        vedtakDvhRepository.lagre(vedtak).apply {
            when {
                this == 1 -> logger.info("Nytt vedtak mottatt og lagret.")
                this > 1 -> logger.error("Vedtak mottatt på nytt. Lagret, merket som duplikat.")
                else -> throw error("Lagring av nytt vedtak mislyktes!")
            }
        }
        secureLogger.info("Vedtak mottatt og lagret: $vedtak")
        ack.acknowledge()
    }
}