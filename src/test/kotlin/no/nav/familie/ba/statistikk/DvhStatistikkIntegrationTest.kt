package no.nav.familie.ba.statistikk

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ba.statistikk.database.DbContainerInitializer
import no.nav.familie.ba.statistikk.domene.VedtakDvhRepository
import no.nav.familie.eksterne.kontrakter.VedtakDVH
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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
        every { repository.lagre(1, any()) } returns 0

         Assertions.assertThrows(IllegalStateException::class.java) {
             VedtakKafkaConsumer(repository)
                     .consume(lagConsumerRecord(TestData.vedtakDhv()), ack)
         }


     }

    @Test
    fun `lagre() returverdi skal telle duplikater`() {
        val vedtak = TestData.vedtakDhv()
        Assertions.assertEquals(1, vedtakDvhRepository.lagre(1, vedtak))
        Assertions.assertEquals(2, vedtakDvhRepository.lagre(1, vedtak))
        Assertions.assertEquals(3, vedtakDvhRepository.lagre(1, vedtak.copy()))
        Assertions.assertEquals(1, vedtakDvhRepository.lagre(1, vedtak.copy(behandlingsId = "2")))
    }

    private fun lagConsumerRecord(vedtak: VedtakDVH): ConsumerRecord<Long, VedtakDVH> {
        return ConsumerRecord("topic", 1, 1, vedtak.behandlingsId.toLong(), vedtak)
    }
}