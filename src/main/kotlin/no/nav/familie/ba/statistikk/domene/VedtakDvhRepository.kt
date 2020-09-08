package no.nav.familie.ba.statistikk.domene

import no.nav.familie.eksterne.kontrakter.VedtakDVH
import no.nav.familie.kontrakter.felles.objectMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class VedtakDvhRepository(private val jdbcTemplate: JdbcTemplate) {

    fun lagre(vedtak: VedtakDVH) {
        jdbcTemplate.update("insert into VEDTAK_DVH(ID, VEDTAK_JSON) values (nextval('VEDTAK_DVH_SEQ'), to_json(?::json))",
                            objectMapper.writeValueAsString(vedtak))
    }

    fun finnes(vedtak: VedtakDVH): Boolean {
        return jdbcTemplate.queryForObject("select count(*) from VEDTAK_DVH where VEDTAK_JSON ->> 'behandlingsId' = ?",
                                    Int::class.java,
                                    vedtak.behandlingsId) > 0
    }
}