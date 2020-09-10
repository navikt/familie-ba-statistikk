package no.nav.familie.ba.statistikk.config

import no.nav.familie.eksterne.kontrakter.VedtakDVH
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.converter.StringJsonMessageConverter
import java.time.Duration

@EnableKafka
@Configuration
class KafkaConfig {

    @Bean
    fun vedtakDvhListenerContainerFactory(properties: KafkaProperties, kafkaErrorHandler: KafkaErrorHandler)
            : ConcurrentKafkaListenerContainerFactory<String, VedtakDVH> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, VedtakDVH>()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        factory.containerProperties.authorizationExceptionRetryInterval = Duration.ofSeconds(2)
        factory.consumerFactory = DefaultKafkaConsumerFactory(properties.buildConsumerProperties())
        factory.setMessageConverter(StringJsonMessageConverter())
        factory.setErrorHandler(kafkaErrorHandler)
        return factory
    }
}
