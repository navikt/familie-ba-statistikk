package no.nav.familie.ba.statistikk.domene

import no.nav.familie.eksterne.kontrakter.VedtakDVH
import no.nav.familie.kontrakter.felles.objectMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class VedtakDvhRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun lagre(offset: Long, vedtak: VedtakDVH): Int {
        val sql =
                "insert into VEDTAK_DVH(ID, VEDTAK_JSON, ER_DUPLIKAT, OFFSET_VERDI) values (nextval('VEDTAK_DVH_SEQ'), to_json(:jsontext::json), :duplikat, :offset)"
        val antallVedtak = antallVedtakMed(vedtak.behandlingsId)
        val parameters = MapSqlParameterSource()
                .addValue("jsontext", objectMapper.writeValueAsString(vedtak))
                .addValue("offset", offset)
                .addValue("duplikat", antallVedtak > 0)

        return jdbcTemplate.update(sql, parameters) + antallVedtak
    }

    fun finnes(vedtak: VedtakDVH): Boolean {
        return antallVedtakMed(vedtak.behandlingsId) > 0
    }

    private fun antallVedtakMed(behandlingsId: String): Int {
        val parameters = MapSqlParameterSource().addValue("behandlingsId", behandlingsId)


        return jdbcTemplate.queryForObject("select count(*) from VEDTAK_DVH where VEDTAK_JSON ->> 'behandlingsId' = :behandlingsId",
                                           parameters,
                                           Int::class.java)!!
    }
}