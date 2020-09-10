package no.nav.familie.ba.statistikk.domene

import no.nav.familie.eksterne.kontrakter.VedtakDVH
import no.nav.familie.kontrakter.felles.objectMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class VedtakDvhRepository(private val jdbcTemplate: JdbcTemplate) {

    fun lagre(vedtak: VedtakDVH): Int {
        val sql = "insert into VEDTAK_DVH(ID, VEDTAK_JSON, ER_DUPLIKAT) values (nextval('VEDTAK_DVH_SEQ'), to_json(?::json), ?)"

        return antallVedtakMed(vedtak.behandlingsId).let {
            jdbcTemplate.update(sql, objectMapper.writeValueAsString(vedtak), it > 0) + it
        }
    }

    fun finnes(vedtak: VedtakDVH): Boolean {
        return antallVedtakMed(vedtak.behandlingsId) > 0
    }

    private fun antallVedtakMed(behandlingsId: String): Int {
        return jdbcTemplate.queryForObject("select count(*) from VEDTAK_DVH where VEDTAK_JSON ->> 'behandlingsId' = ?",
                                           Int::class.java,
                                           behandlingsId)
    }
}