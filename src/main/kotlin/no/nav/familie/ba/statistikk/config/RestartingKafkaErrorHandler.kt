package no.nav.familie.ba.statistikk.config

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.kafka.listener.ContainerStoppingErrorHandler
import org.springframework.kafka.listener.MessageListenerContainer
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.Executor

@Component
class RestartingKafkaErrorHandler : ContainerStoppingErrorHandler() {

    val LOGGER: Logger = LoggerFactory.getLogger(RestartingKafkaErrorHandler::class.java)
    val SECURE_LOGGER: Logger = LoggerFactory.getLogger("secureLogger")

    private val executor: Executor
    override fun handle(
        e: Exception,
        records: List<ConsumerRecord<*, *>>?,
        consumer: Consumer<*, *>,
        container: MessageListenerContainer
    ) {
        Thread.sleep(1000)

        if (records.isNullOrEmpty()) {
            LOGGER.error("Feil ved konsumering av melding. Ingen records. ${consumer.subscription()}", e)
            scheduleRestart(
                e,
                records,
                consumer,
                container,
                "Ukjent topic"
            )
        } else {
            records.first().run {
                LOGGER.error(
                    "Feil ved konsumering av melding fra ${this.topic()}. id ${this.key()}, " +
                            "offset: ${this.offset()}, partition: ${this.partition()}"
                )
                SECURE_LOGGER.error("${this.topic()} - Problemer med prosessering av $records", e)
                scheduleRestart(
                    e,
                    records,
                    consumer,
                    container,
                    this.topic()
                )
            }
        }
    }

    private fun scheduleRestart(
        e: Exception,
        records: List<ConsumerRecord<*, *>>? = null,
        consumer: Consumer<*, *>,
        container: MessageListenerContainer,
        topic: String
    ) {
        executor.execute {
            try {
                Thread.sleep(SHORT)
                LOGGER.warn("Starter kafka container for {}", topic)
                container.start()
            } catch (exception: Exception) {
                LOGGER.error("Feil oppstod ved venting og oppstart av kafka container", exception)
            }
        }
        LOGGER.warn("Stopper kafka container for {} i {}", topic, Duration.ofMillis(SHORT).toString())
        super.handle(e, records, consumer, container)
    }

    companion object {

        private val SHORT = Duration.ofSeconds(10).toMillis()
    }

    init {
        this.executor = SimpleAsyncTaskExecutor()
    }

}