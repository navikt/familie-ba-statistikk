package no.nav.familie.ba.statistikk

import io.mockk.*
import no.nav.familie.ba.statistikk.database.DbContainerInitializer
import no.nav.familie.ba.statistikk.domene.VedtakDvhRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
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
        vedtakKafkaConsumer.consume(vedtak, ack)

        Assertions.assertTrue(vedtakDvhRepository.finnes(vedtak))
    }

    @Test
    fun `consume() skal kaste error hvis lagre() returnerer 0`() {
        val jdbcTemplate: JdbcTemplate = mockk()
        every { jdbcTemplate.update(any(), any(), false) } returns 0
        every { jdbcTemplate.queryForObject(any(), Int::class.java, any()) } returns 0

        Assertions.assertThrows(IllegalStateException::class.java) {
            VedtakKafkaConsumer(VedtakDvhRepository(jdbcTemplate))
                    .consume(TestData.vedtakDhv(), ack)
        }
    }

    @Test
    fun `lagre() returverdi skal telle duplikater`() {
        val vedtak = TestData.vedtakDhv()
        Assertions.assertEquals(1, vedtakDvhRepository.lagre(vedtak))
        Assertions.assertEquals(2, vedtakDvhRepository.lagre(vedtak))
        Assertions.assertEquals(3, vedtakDvhRepository.lagre(vedtak.copy()))
        Assertions.assertEquals(1, vedtakDvhRepository.lagre(vedtak.copy(behandlingsId = "2")))
    }
}