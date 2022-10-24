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

    @KafkaListener(topics = [TOPIC_NAVN],
                   groupId = "saksstatistikk-sak-1",
                   id = "familie-ba-saksstatistikk-sak-1",
                   idIsGroup = false,
                   containerFactory = "kafkaAivenHendelseListenerContainerFactory",
                   autoStartup = "\${kafka.enabled:true}"
    )
    @Transactional
    fun consume(cr: ConsumerRecord<String, String>, ack: Acknowledgment) {
        try {
            val offset = cr.offset()
            val json = cr.value()
            val key = cr.key()

            logger.info("[${SAK}] Melding mottatt. offset=$offset, key=$key")
            secureLogger.info("[${SAK}] Melding mottatt. offset=$offset, key=$key, melding=$json")

            if (saksstatistikkDvhRepository.harLestSakMelding(key, offset)) {
                logger.info("har alt lest $SAK-melding med key $key og offset $offset")
                ack.acknowledge()
                return
            }

            saksstatistikkDvhRepository.lagre(SAK, offset, json, funksjonellId = key).apply {
                when {
                    this == 1 -> secureLogger.info("$SAK-melding mottatt og lagret: $json")
                    this > 1 -> logger.error("$SAK-melding mottatt på nytt. Lagret, merket som duplikat. offset=$offset key=$key")
                    else -> error("Lagring av ny $SAK-melding mislyktes! offset=$offset key=$key")
                }
            }
            //valider at meldingen lar seg deserialisere
            try {
                objectMapper.readValue(json, SakDVH::class.java)
            } catch (e: Exception) {
                logger.error("json for $SAK kan ikke parses til nyeste SakDVH", e)
                secureLogger.error("json for $SAK kan ikke parses til nyeste SakDVH \n$json")
            }
            validerSakDvhMotJsonSchema(json)

        } catch (up: Exception) {
            handleException(up, cr, logger, SAK)
        }
        ack.acknowledge()
    }

    companion object {
        private const val SAK = "SAK"
        const val TOPIC_NAVN = "teamfamilie:aapen-barnetrygd-saksstatistikk-sak-v1"
    }
}