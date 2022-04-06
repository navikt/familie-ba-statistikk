package no.nav.familie.ba.statistikk.domene

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class SaksstatistikkDvhRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun lagre(type: String, offset: Long, json: String, funksjonellId: String): Int {
        val sql =
                "insert into SAKSSTATISTIKK_DVH(ID, JSON, OFFSET_VERDI, TYPE, FUNKSJONELL_ID) " +
                "values (nextval('SAKSSTATISTIKK_DVH_SEQ'), to_json(:jsontext::json), :offset, :type, :funksjonellId)"
        val antallHendelserMedFunksjonellId = antallHendelserMedFunksjonellId(funksjonellId)
        val parameters = MapSqlParameterSource()
                .addValue("jsontext", json)
                .addValue("offset", offset)
                .addValue("duplikat", antallHendelserMedFunksjonellId > 0)
                .addValue("type", type)
                .addValue("funksjonellId", funksjonellId)

        return jdbcTemplate.update(sql, parameters) + antallHendelserMedFunksjonellId
    }

    private fun antallHendelserMedFunksjonellId(funksjonellId: String): Int {
        val parameters = MapSqlParameterSource().addValue("funksjonellId", funksjonellId)


        return jdbcTemplate.queryForObject("select count(*) from SAKSSTATISTIKK_DVH where FUNKSJONELL_ID = :funksjonellId",
                                           parameters,
                                           Int::class.java)!!
    }

    fun hent(type: String, offset: Long): String {
        val parameters = MapSqlParameterSource()
                .addValue("offset", offset)
                .addValue("type", type)

        return jdbcTemplate.queryForObject("""SELECT s.json FROM saksstatistikk_dvh s WHERE s.offset_verdi = :offset
                                                   AND s.type = :type""",
                                           parameters,
                                           String::class.java)!!
    }

    fun antallBehandlingerMedId(behandlingId: String): Int {
        val parameters = MapSqlParameterSource().addValue("behandlingId", behandlingId)


        return jdbcTemplate.queryForObject("select count(*) from SAKSSTATISTIKK_DVH where type = 'BEHANDLING' AND JSON ->> 'behandlingId' = :behandlingId",
                                           parameters,
                                           Int::class.java)!!
    }

    fun antallFagsakMedId(fagsakId: String): Int {
        val parameters = MapSqlParameterSource().addValue("fagsakId", fagsakId)


        return jdbcTemplate.queryForObject("select count(*) from SAKSSTATISTIKK_DVH where type = 'SAK' AND JSON ->> 'fagsakId' = :fagsakId",
                                           parameters,
                                           Int::class.java)!!
    }
}