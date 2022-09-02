package no.nav.familie.ba.statistikk.config


import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.converter.StringJsonMessageConverter
import java.time.Duration

@EnableKafka
@Configuration
class KafkaConfig {

    @Bean
    fun vedtakDvhListenerContainerFactory(properties: KafkaProperties, restartingKafkaErrorHandler: RestartingKafkaErrorHandler)
            : ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        factory.containerProperties.setAuthExceptionRetryInterval(Duration.ofSeconds(2))
        factory.consumerFactory = DefaultKafkaConsumerFactory(properties.buildConsumerProperties())
        factory.setMessageConverter(StringJsonMessageConverter())
        factory.setCommonErrorHandler(restartingKafkaErrorHandler)
        return factory
    }


    @Bean
    fun kafkaOnpremProducer(producerFactory: ProducerFactory<String, String>): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory)
    }
}
