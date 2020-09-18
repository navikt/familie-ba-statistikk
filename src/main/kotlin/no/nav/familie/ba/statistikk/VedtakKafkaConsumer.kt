package no.nav.familie.ba.statistikk

import com.fasterxml.jackson.core.JsonProcessingException
import no.nav.familie.ba.statistikk.domene.VedtakDvhRepository
import no.nav.familie.eksterne.kontrakter.VedtakDVH
import no.nav.familie.kontrakter.felles.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
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
    fun consume(cr: ConsumerRecord<String, String>, ack: Acknowledgment) {
        try {
            val vedtak = cr.value()

            vedtakDvhRepository.lagre(cr.offset(), cr.key(), vedtak).apply {
                when {
                    this == 1 -> logger.info("Nytt vedtak mottatt og lagret.")
                    this > 1 -> logger.error("Vedtak mottatt på nytt. Lagret, merket som duplikat. offset=${cr.offset()} key=${cr.key()}")
                    else -> throw error("Lagring av nytt vedtak mislyktes! offset=${cr.offset()} key=${cr.key()}")
                }
            }


            secureLogger.info("Vedtak mottatt og lagret: $vedtak")

            //valider at meldingen lar seg deserialisere
            objectMapper.readValue(vedtak, VedtakDVH::class.java)

        } catch (up: Exception) {

            when(up) {
                is DataIntegrityViolationException -> {
                    logger.error("Fikk melding som ikke var gyldig json, hopper over melding. offset=${cr.offset()} key=${cr.key()}")
                    secureLogger.error("Fikk melding som ikke var gyldig json. offset=${cr.offset()} key=${cr.key()} melding=${cr.value()}", up)

                }
                is JsonProcessingException -> {
                    logger.error("Fikk melding som ikke er i henhold til gjeldende kontrakt. offset=${cr.offset()} key=${cr.key()}")
                    secureLogger.error("Fikk melding som ikke er i henhold til gjeldende kontrakt. offset=${cr.offset()} key=${cr.key()} melding=${cr.value()}", up)
                    //Må ta ibruk ny kontrakt for å kunne lese disse meldingene
                    throw up
                }
                else -> throw up
            }
        }
        ack.acknowledge()
    }
}