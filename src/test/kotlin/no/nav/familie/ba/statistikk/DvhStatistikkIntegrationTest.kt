package no.nav.familie.ba.statistikk

import com.fasterxml.jackson.core.JsonProcessingException
import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ba.statistikk.database.DbContainerInitializer
import no.nav.familie.ba.statistikk.domene.VedtakDVHType
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
    lateinit var saksstatistikkBehandlingConsumer: SaksstatistikkBehandlingConsumer

    @Autowired
    lateinit var vedtak2KafkaConsumer: Vedtak2KafkaConsumer

    @Autowired
    lateinit var sakConsumer: SaksstatistikkSakConsumer

    @Autowired
    lateinit var jdbcTemplate: NamedParameterJdbcTemplate


    private val ack: Acknowledgment = mockk(relaxed = true)

    @Test
    fun `consume() skal lagre vedtak til database`() {
        val vedtak = TestData.vedtakDvhV2()

        vedtak2KafkaConsumer.consume(lagConsumerRecord(vedtak), ack)
        vedtak2KafkaConsumer.consume(lagConsumerRecord(vedtak), ack)

        Assertions.assertTrue(vedtakDvhRepository.finnes(vedtak.behandlingsId))
    }

    @Test
    fun `skal lagre saksstatistikk til database`() {
        val sak = TestData.sakDvh()
        val parameters = MapSqlParameterSource().addValue("sakId", sak.sakId)

        val init_count =
                jdbcTemplate.queryForObject("select count(*) from SAKSSTATISTIKK_DVH where type='SAK_2' and JSON ->> 'sakId' = :sakId",
                                            parameters,
                                            Int::class.java)!!

        sakConsumer.consume(lagConsumerRecord(sak), ack)

        val count: Int =
                jdbcTemplate.queryForObject("select count(*) from SAKSSTATISTIKK_DVH where type='SAK_2' and JSON ->> 'sakId' = :sakId",
                                            parameters,
                                            Int::class.java)!!

        Assertions.assertTrue(count > init_count)

    }

    @Test
    fun `skal lagre saksstatistikk for behandling til database`() {
        val behandling = TestData.behandlingDvh()
        val parameters = MapSqlParameterSource().addValue("behandlingId", behandling.behandlingId)

        val init_count =
                jdbcTemplate.queryForObject("select count(*) from SAKSSTATISTIKK_DVH where type='BEHANDLING_2' and JSON ->> 'behandlingId' = :behandlingId",
                                            parameters,
                                            Int::class.java)!!

        saksstatistikkBehandlingConsumer.consume(lagConsumerRecord(behandling), ack)

        val count: Int =
                jdbcTemplate.queryForObject("select count(*) from SAKSSTATISTIKK_DVH where type='BEHANDLING_2' and JSON ->> 'behandlingId' = :behandlingId",
                                            parameters,
                                            Int::class.java)!!

        Assertions.assertTrue(count > init_count)

    }

    @Test
    fun `consume() skal kaste error hvis lagre() returnerer 0`() {
        val repository: VedtakDvhRepository = mockk()
        every { repository.lagre(1, any(), VedtakDVHType.VEDTAK_V2, any(), any()) } returns 0
        every { repository.harLestMeldiong(1) } returns false

        Assertions.assertThrows(IllegalStateException::class.java) {
            Vedtak2KafkaConsumer(repository)
                    .consume(lagConsumerRecord(TestData.vedtakDvhV2()), ack)
        }


    }

    @Test
    fun `consume() skal kaste error hvis json ikke er i henhold til kontrakt`() {
        val repository: VedtakDvhRepository = mockk()
        every { repository.lagre(1, any(), VedtakDVHType.VEDTAK_V2, any(), null) } returns 1
        every { repository.harLestMeldiong(1) } returns false

        Assertions.assertThrows(JsonProcessingException::class.java) {
            Vedtak2KafkaConsumer(repository)
                    .consume(lagConsumerRecord("{\"sdf\":\"sdf\"}"), ack)
        }


    }

    @Test
    fun `lagre() returverdi skal telle duplikater`() {
        val vedtak = TestData.vedtakDvhV2()
        val vedtakJson = objectMapper.writeValueAsString(vedtak)
        Assertions.assertEquals(1, vedtakDvhRepository.lagre(1, vedtakJson, VedtakDVHType.VEDTAK_V2, vedtak.behandlingsId, vedtak.funksjonellId))
        Assertions.assertEquals(2, vedtakDvhRepository.lagre(1, vedtakJson, VedtakDVHType.VEDTAK_V2, vedtak.behandlingsId, vedtak.funksjonellId))
        Assertions.assertEquals(3, vedtakDvhRepository.lagre(1, vedtakJson, VedtakDVHType.VEDTAK_V2, vedtak.behandlingsId, vedtak.funksjonellId))
        Assertions.assertEquals(1, vedtakDvhRepository.lagre(
            1, vedtakJson,
            VedtakDVHType.VEDTAK_V2,
            "2",
            vedtak.funksjonellId
        ))
    }

    @Test
    fun `lagre() skal kaste feil hvis innhold i melding ikke er json`() {
        val vedtak = TestData.vedtakDvhV2()
        val vedtakJson = "FOO"
        Assertions.assertThrows(DataIntegrityViolationException::class.java) {
            vedtakDvhRepository.lagre(1, vedtakJson, VedtakDVHType.VEDTAK_V2, vedtak.behandlingsId, vedtak.funksjonellId)
        }
    }


    private fun lagConsumerRecord(obj: Any): ConsumerRecord<String, String> {
        return ConsumerRecord("topic", 1, 1, "1", objectMapper.writeValueAsString(obj))
    }

    private fun lagConsumerRecord(vedtak: String): ConsumerRecord<String, String> {
        return ConsumerRecord("topic", 1, 1, "1", vedtak)
    }
}