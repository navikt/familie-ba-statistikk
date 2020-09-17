package no.nav.familie.ba.statistikk

import com.fasterxml.jackson.core.JsonProcessingException
import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ba.statistikk.database.DbContainerInitializer
import no.nav.familie.ba.statistikk.domene.VedtakDvhRepository
import no.nav.familie.eksterne.kontrakter.VedtakDVH
import no.nav.familie.kontrakter.felles.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.kafka.support.Acknowledgment
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(initializers = [DbContainerInitializer::class])
@ActiveProfiles("postgres")
@Tag("integration")
@SpringBootTest(classes = [DevLauncher::class])
class DvhStatistikkIntegrationTest {

    @Autowired
    lateinit var vedtakDvhRepository: VedtakDvhRepository

    @Autowired
    lateinit var vedtakKafkaConsumer: VedtakKafkaConsumer

    private val ack: Acknowledgment = mockk(relaxed = true)

    @Test
    fun `consume() skal lagre vedtak til database`() {
        val vedtak = TestData.vedtakDhv()

        vedtakKafkaConsumer.consume(lagConsumerRecord(vedtak), ack)

        Assertions.assertTrue(vedtakDvhRepository.finnes(vedtak))
    }

    @Test
     fun `consume() skal kaste error hvis lagre() returnerer 0`() {
        val repository: VedtakDvhRepository = mockk()
        every { repository.lagre(1, any(), any()) } returns 0

         Assertions.assertThrows(IllegalStateException::class.java) {
             VedtakKafkaConsumer(repository)
                     .consume(lagConsumerRecord(TestData.vedtakDhv()), ack)
         }


     }

    @Test
    fun `consume() skal kaste error hvis json ikke er i henhold til kontrakt`() {
        val repository: VedtakDvhRepository = mockk()
        every { repository.lagre(1, any(), any()) } returns 1

        Assertions.assertThrows(JsonProcessingException::class.java) {
            VedtakKafkaConsumer(repository)
                    .consume(lagConsumerRecord("{\"sdf\":\"sdf\"}"), ack)
        }


    }

    @Test
    fun `consume() skal ikke kaste error hvis vi får inn ugyldig json`() {

        Assertions.assertDoesNotThrow{
                VedtakKafkaConsumer(vedtakDvhRepository).consume(lagConsumerRecord(""), ack)}

    }

    @Test
    fun `lagre() returverdi skal telle duplikater`() {
        val vedtak = TestData.vedtakDhv()
        val vedtakJson = objectMapper.writeValueAsString(vedtak)
        Assertions.assertEquals(1, vedtakDvhRepository.lagre(1, vedtak.behandlingsId, vedtakJson))
        Assertions.assertEquals(2, vedtakDvhRepository.lagre(1, vedtak.behandlingsId, vedtakJson))
        Assertions.assertEquals(3, vedtakDvhRepository.lagre(1, vedtak.behandlingsId, vedtakJson))
        Assertions.assertEquals(1, vedtakDvhRepository.lagre(1, vedtak.copy(behandlingsId = "2").behandlingsId,
                                                             vedtakJson))
    }

    @Test
    fun `lagre() skal kaste feil hvis innhold i melding ikke er json`() {
        val vedtak = TestData.vedtakDhv()
        val vedtakJson = "FOO"
        Assertions.assertThrows(DataIntegrityViolationException::class.java) {
            vedtakDvhRepository.lagre(1, vedtak.behandlingsId, vedtakJson)
        }
    }


    private fun lagConsumerRecord(vedtak: VedtakDVH): ConsumerRecord<String, String> {
        return ConsumerRecord("topic", 1, 1, vedtak.behandlingsId, objectMapper.writeValueAsString(vedtak))
    }

    private fun lagConsumerRecord(vedtak: String): ConsumerRecord<String, String> {
        return ConsumerRecord("topic", 1, 1, "1", vedtak)
    }
}