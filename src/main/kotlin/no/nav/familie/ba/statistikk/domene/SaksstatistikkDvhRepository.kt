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

    fun hent(type: String, offset: Long, fraAiven: Boolean = true): String {
        val parameters = MapSqlParameterSource()
                .addValue("offset", offset)
                .addValue("type", type)
                .addValue("fraAiven", fraAiven)

        return jdbcTemplate.queryForObject("""SELECT s.json FROM saksstatistikk_dvh s WHERE s.offset_verdi = :offset
                                                   AND s.type = :type
                                                   AND s.fra_aiven = :fraAiven""",
                                           parameters,
                                           String::class.java)!!
    }

    fun antallBehandlingerMedId(behandlingId: String): Int {
        val parameters = MapSqlParameterSource().addValue("behandlingId", behandlingId)


        return jdbcTemplate.queryForObject("select count(*) from SAKSSTATISTIKK_DVH where type = 'BEHANDLING' AND JSON ->> 'behandlingId' = :behandlingId",
                                           parameters,
                                           Int::class.java)!!
    }

    fun hentJSONForBehandlingerMedBehandlingId(behandlingId: String): List<String> {
        val parameters = MapSqlParameterSource().addValue("behandlingId", behandlingId)


        return jdbcTemplate.queryForList("select json from SAKSSTATISTIKK_DVH where type = 'BEHANDLING' AND JSON ->> 'behandlingId' = :behandlingId",
                                           parameters,
                                           String::class.java)!!
    }

    fun hentJSONBehandlingerMedFunksjonellId(funksjonellId: String): List<String> {
        val parameters = MapSqlParameterSource().addValue("funksjonellId", funksjonellId)


        return jdbcTemplate.queryForList("select json from SAKSSTATISTIKK_DVH where type = 'BEHANDLING' AND funksjonell_id = :funksjonellId",
                                           parameters,
                                           String::class.java)!!
    }

    fun antallFagsakMedId(fagsakId: String): Int {
        val parameters = MapSqlParameterSource().addValue("fagsakId", fagsakId)


        return jdbcTemplate.queryForObject("select count(*) from SAKSSTATISTIKK_DVH where type = 'SAK' AND JSON ->> 'sakId' = :fagsakId",
                                           parameters,
                                           Int::class.java)!!
    }

    fun hentJSONForFagsakForFagsakId(fagsakId: String): List<String>  {
        val parameters = MapSqlParameterSource().addValue("fagsakId", fagsakId)


        return jdbcTemplate.queryForList("select json from SAKSSTATISTIKK_DVH where type = 'SAK' AND JSON ->> 'sakId' = :fagsakId",
                                           parameters,
                                           String::class.java)!!
    }

    fun hentJSONForFagsakForFunksjonellId(funksjonellId: String): List<String>  {
        val parameters = MapSqlParameterSource().addValue("funksjonellId", funksjonellId)


        return jdbcTemplate.queryForList("select json from SAKSSTATISTIKK_DVH where type = 'SAK' AND funksjonell_id = :funksjonellId",
                                           parameters,
                                           String::class.java)!!
    }

    fun harLestSakMelding(funksjonellId: String, offset: Long): Boolean {
        val parameters = MapSqlParameterSource()
            .addValue("funksjonellId", funksjonellId)
            .addValue("offset", offset)

        return jdbcTemplate.queryForObject("""select count(*) from SAKSSTATISTIKK_DVH where type = 'SAK'
                                               and funksjonell_id = :funksjonellId
                                               and offset_verdi = :offset
                                               and fra_aiven = true""".trimMargin(),
                                           parameters,
                                           Int::class.java)!! > 0
    }

    fun harLestBehandlingMelding(funksjonellId: String, offset: Long): Boolean {
        val parameters = MapSqlParameterSource()
            .addValue("funksjonellId", funksjonellId)
            .addValue("offset", offset)

        return jdbcTemplate.queryForObject("""select count(*) from SAKSSTATISTIKK_DVH where type = 'BEHANDLING'
                                               and funksjonell_id = :funksjonellId
                                               and offset_verdi = :offset
                                               and fra_aiven = true""".trimMargin(),
                                           parameters,
                                           Int::class.java)!! > 0
    }

}