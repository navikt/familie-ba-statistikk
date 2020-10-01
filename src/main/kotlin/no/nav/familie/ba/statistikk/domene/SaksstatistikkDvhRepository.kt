package no.nav.familie.ba.statistikk.domene

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class SaksstatistikkDvhRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun lagre(type: String, offset: Long, json: String): Int {
        val sql =
                "insert into SAKSSTATISTIKK_DVH(ID, JSON, OFFSET_VERDI, TYPE) values (nextval('SAKSSTATISTIKK_DVH_SEQ'), to_json(:jsontext::json), :offset, :type)"
        val parameters = MapSqlParameterSource()
                .addValue("jsontext", json)
                .addValue("offset", offset)
//                .addValue("duplikat",       false) //TODO Hva definerer duplikat for sak- og behandlinghendelser
                .addValue("type", type)

        return jdbcTemplate.update(sql, parameters)
    }
}