package no.nav.familie.ba.statistikk.domene

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class VedtakDvhRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun lagre(offset: Long, vedtakJson: String, type: VedtakDVHType, behandlingId: String, funksjonellId: String?): Int {
        val sql =
                "insert into VEDTAK_DVH(ID, VEDTAK_JSON, ER_DUPLIKAT, OFFSET_VERDI, FUNKSJONELL_ID, TYPE) " +
                "values (nextval('VEDTAK_DVH_SEQ'), to_json(:jsontext::json), :duplikat, :offset, :funksjonellId, :type)"
        val antallVedtak = antallVedtakMed(behandlingId)
        val parameters = MapSqlParameterSource()
                .addValue("jsontext", vedtakJson)
                .addValue("offset", offset)
                .addValue("duplikat", antallVedtak > 0)
                .addValue("funksjonellId", funksjonellId)
                .addValue("type", type.name)

        return jdbcTemplate.update(sql, parameters) + antallVedtak
    }

    fun finnes(behandlingsId: String): Boolean {
        return antallVedtakMed(behandlingsId) > 0
    }


    private fun antallVedtakMed(behandlingsId: String): Int {
        val parameters = MapSqlParameterSource().addValue("behandlingsId", behandlingsId)


        return jdbcTemplate.queryForObject("select count(*) from VEDTAK_DVH where VEDTAK_JSON ->> 'behandlingsId' = :behandlingsId",
                                           parameters,
                                           Int::class.java)!!
    }
}

enum class VedtakDVHType {
    VEDTAK_V1,
    VEDTAK_V2
}