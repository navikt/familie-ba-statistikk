package no.nav.familie.ba.statistikk

import com.fasterxml.jackson.core.JsonProcessingException
import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ba.statistikk.database.DbContainerInitializer
import no.nav.familie.ba.statistikk.domene.SaksstatistikkDvhRepository
import no.nav.familie.ba.statistikk.domene.VedtakDvhRepository
import no.nav.familie.kontrakter.felles.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
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

    @Autowired
    lateinit var saksstatistikkDvhRepository: SaksstatistikkDvhRepository

    @Autowired
    lateinit var saksstatistikkBehandlingConsumer: SaksstatistikkBehandlingConsumer

    @Autowired
    lateinit var sakConsumer: SaksstatistikkSakConsumer

    @Autowired
    lateinit var jdbcTemplate: NamedParameterJdbcTemplate


    private val ack: Acknowledgment = mockk(relaxed = true)

    @Test
    fun `consume() skal lagre vedtak til database`() {
        val vedtak = TestData.vedtakDvh()

        vedtakKafkaConsumer.consume(lagConsumerRecord(vedtak), ack)

        Assertions.assertTrue(vedtakDvhRepository.finnes(vedtak))
    }

    @Test
    fun `skal lagre saksstatistikk til database`() {
        val sak = TestData.sakDvh()
        val parameters = MapSqlParameterSource().addValue("sakId", sak.sakId)

        val init_count =
                jdbcTemplate.queryForObject("select count(*) from SAKSSTATISTIKK_DVH where type='SAK' and JSON ->> 'sakId' = :sakId",
                                            parameters,
                                            Int::class.java)!!

        sakConsumer.consume(lagConsumerRecord(sak), ack)

        val count: Int =
                jdbcTemplate.queryForObject("select count(*) from SAKSSTATISTIKK_DVH where type='SAK' and JSON ->> 'sakId' = :sakId",
                                            parameters,
                                            Int::class.java)!!

        Assertions.assertTrue(count > init_count)

    }

    @Test
    fun `skal lagre saksstatistikk for behandling til database`() {
        val behandling = TestData.behandlingDvh()
        val parameters = MapSqlParameterSource().addValue("behandlingId", behandling.behandlingId)

        val init_count =
                jdbcTemplate.queryForObject("select count(*) from SAKSSTATISTIKK_DVH where type='BEHANDLING' and JSON ->> 'behandlingId' = :behandlingId",
                                            parameters,
                                            Int::class.java)!!

        saksstatistikkBehandlingConsumer.consume(lagConsumerRecord(behandling), ack)

        val count: Int =
                jdbcTemplate.queryForObject("select count(*) from SAKSSTATISTIKK_DVH where type='BEHANDLING' and JSON ->> 'behandlingId' = :behandlingId",
                                            parameters,
                                            Int::class.java)!!

        Assertions.assertTrue(count > init_count)

    }

    @Test
     fun `consume() skal kaste error hvis lagre() returnerer 0`() {
        val repository: VedtakDvhRepository = mockk()
        every { repository.lagre(1, any(), any()) } returns 0

         Assertions.assertThrows(IllegalStateException::class.java) {
             VedtakKafkaConsumer(repository)
                     .consume(lagConsumerRecord(TestData.vedtakDvh()), ack)
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
        val vedtak = TestData.vedtakDvh()
        val vedtakJson = objectMapper.writeValueAsString(vedtak)
        Assertions.assertEquals(1, vedtakDvhRepository.lagre(1, vedtak.behandlingsId, vedtakJson))
        Assertions.assertEquals(2, vedtakDvhRepository.lagre(1, vedtak.behandlingsId, vedtakJson))
        Assertions.assertEquals(3, vedtakDvhRepository.lagre(1, vedtak.behandlingsId, vedtakJson))
        Assertions.assertEquals(1, vedtakDvhRepository.lagre(1, vedtak.copy(behandlingsId = "2").behandlingsId,
                                                             vedtakJson))
    }

    @Test
    fun `lagre() skal kaste feil hvis innhold i melding ikke er json`() {
        val vedtak = TestData.vedtakDvh()
        val vedtakJson = "FOO"
        Assertions.assertThrows(DataIntegrityViolationException::class.java) {
            vedtakDvhRepository.lagre(1, vedtak.behandlingsId, vedtakJson)
        }
    }


    private fun lagConsumerRecord(obj: Any): ConsumerRecord<String, String> {
        return ConsumerRecord("topic", 1, 1, "1", objectMapper.writeValueAsString(obj))
    }

    private fun lagConsumerRecord(vedtak: String): ConsumerRecord<String, String> {
        return ConsumerRecord("topic", 1, 1, "1", vedtak)
    }
}