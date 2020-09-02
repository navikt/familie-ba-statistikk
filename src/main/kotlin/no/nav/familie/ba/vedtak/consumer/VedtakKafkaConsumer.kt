package no.nav.familie.ba.vedtak.consumer

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VedtakKafkaConsumer {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @KafkaListener(topics = ["aapen-barnetrygd-vedtak-v1"],
                   id = "familie-ba-statistikk",
                   idIsGroup = false,
                   containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    fun listen(cr: ConsumerRecord<String, String>, ack: Acknowledgment) {
        logger.info("Vedtak mottatt")
        secureLogger.info("Vedtak mottatt: $cr")
    }
}