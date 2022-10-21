package no.nav.familie.ba.statistikk

import com.fasterxml.jackson.databind.JsonNode
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

    @KafkaListener(topics = [TOPIC_NAVN],
                   groupId = "saksstatistikk-behandling-1",
                   id = "familie-ba-saksstatistikk-behandling-1",
                   idIsGroup = false,
                   containerFactory = "kafkaAivenHendelseListenerContainerFactory",
                   autoStartup = "\${kafka.enabled:true}"
    )
    @Transactional
    fun consume(cr: ConsumerRecord<String, String>, ack: Acknowledgment) {
        try {
            val offset = cr.offset()

            logger.info("[${BEHANDLING}] Melding mottatt. offset=$offset, key=${cr.key()}")
            secureLogger.info("[${BEHANDLING}] Melding mottatt. offset=$offset, key=${cr.key()}, melding=${cr.value()}")

            if (saksstatistikkDvhRepository.harLestBehandlingMelding(offset)) {
                logger.info("har alt lest $BEHANDLING-melding med offset $offset")
                ack.acknowledge()
                return
            }

            val json = cr.value()
            val parent: JsonNode = objectMapper.readTree(json)
            val funksjonellId: String = parent.path("funksjonellId").asText()

            saksstatistikkDvhRepository.lagre(BEHANDLING, offset, json, funksjonellId).apply {
                when {
                    this == 1 -> secureLogger.info("$BEHANDLING-melding mottatt og lagret: $json")
                    this > 1 -> logger.error("$BEHANDLING-melding mottatt på nytt. Lagret, merket som duplikat. offset=$offset key=${cr.key()}")
                    else -> error("Lagring av ny $BEHANDLING-melding mislyktes! offset=$offset key=${cr.key()}")
                }
            }
            //valider at meldingen lar seg deserialisere
            try {
                objectMapper.readValue(json, BehandlingDVH::class.java)
            } catch (e: Exception) {
                logger.error("json for $BEHANDLING kan ikke parses til nyeste BehandlingDVH", e)
                secureLogger.error("json for $BEHANDLING kan ikke parses til nyeste BehandlingDVH \n$json")
            }
            validerBehandlingDvhMotJsonSchema(json)

        } catch (up: Exception) {
            handleException(up, cr, logger, BEHANDLING)
        }
        ack.acknowledge()
    }

    companion object {
        private const val BEHANDLING = "BEHANDLING"
        const val TOPIC_NAVN = "teamfamilie:aapen-barnetrygd-saksstatistikk-behandling-v1"
    }
}