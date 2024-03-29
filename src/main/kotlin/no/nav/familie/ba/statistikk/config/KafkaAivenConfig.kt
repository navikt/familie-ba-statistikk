package no.nav.familie.ba.statistikk.config

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerConfigUtils
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ContainerProperties
import java.time.Duration


@Configuration
class KafkaAivenConfig(val environment: Environment) {

    @Bean
    fun kafkaAivenHendelseListenerContainerFactory(kafkaRestartingErrorHandler: RestartingKafkaErrorHandler)
            : ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        factory.containerProperties.setAuthExceptionRetryInterval(Duration.ofSeconds(2))
        factory.consumerFactory = DefaultKafkaConsumerFactory(consumerConfigs())
        factory.setCommonErrorHandler(kafkaRestartingErrorHandler)
        return factory
    }

    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        return DefaultKafkaProducerFactory(producerConfigs())
    }

    @Bean
    fun kafkaAivenTemplate(): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory())
    }

    @ConditionalOnMissingBean(KafkaListenerEndpointRegistry::class)
    @Bean(name = [KafkaListenerConfigUtils.KAFKA_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME])
    fun kafkaListenerEndpointRegistry(): KafkaListenerEndpointRegistry {
        return KafkaListenerEndpointRegistry()
    }

    private fun consumerConfigs(): Map<String, Any> {
        val kafkaBrokers = System.getenv("KAFKA_BROKERS") ?: "http://localhost:9092"
        val consumerConfigs = mutableMapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBrokers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.CLIENT_ID_CONFIG to "consumer-familie-ba-statistikk-1",
            ConsumerConfig.MAX_POLL_RECORDS_CONFIG to "100",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest"
        )
        if (environment.activeProfiles.none { it.contains("dev") || it.contains("postgres") }) {
            return consumerConfigs + securityConfig()
        }
        return consumerConfigs.toMap()
    }

    private fun producerConfigs(): Map<String, Any> {
        val kafkaBrokers = System.getenv("KAFKA_BROKERS") ?: "http://localhost:9092"
        val producerConfigs = mutableMapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBrokers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true, // Den sikrer rekkefølge
            ProducerConfig.ACKS_CONFIG to "all", // Den sikrer at data ikke mistes
            ProducerConfig.CLIENT_ID_CONFIG to "producer-familie-ba-statistikk-1"
        )
        if (environment.activeProfiles.none { it.contains("dev") || it.contains("postgres") }) {
            return producerConfigs + securityConfig()
        }
        return producerConfigs.toMap()
    }

    private fun securityConfig(): Map<String, Any> {
        val kafkaTruststorePath = System.getenv("KAFKA_TRUSTSTORE_PATH")
        val kafkaCredstorePassword = System.getenv("KAFKA_CREDSTORE_PASSWORD")
        val kafkaKeystorePath = System.getenv("KAFKA_KEYSTORE_PATH")
        return mapOf(
            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SSL",
            SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG to "", // Disable server host name verification
            SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG to "JKS",
            SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to "PKCS12",
            SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to kafkaTruststorePath,
            SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to kafkaCredstorePassword,
            SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to kafkaKeystorePath,
            SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to kafkaCredstorePassword,
            SslConfigs.SSL_KEY_PASSWORD_CONFIG to kafkaCredstorePassword
        )
    }
}
