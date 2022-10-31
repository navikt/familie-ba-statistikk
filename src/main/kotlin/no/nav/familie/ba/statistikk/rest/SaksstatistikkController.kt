package no.nav.familie.ba.statistikk.rest

import no.nav.familie.ba.statistikk.domene.SaksstatistikkDVHType.BEHANDLING
import no.nav.familie.ba.statistikk.domene.SaksstatistikkDVHType.BEHANDLING_2
import no.nav.familie.ba.statistikk.domene.SaksstatistikkDVHType.SAK
import no.nav.familie.ba.statistikk.domene.SaksstatistikkDVHType.SAK_2
import no.nav.familie.ba.statistikk.domene.SaksstatistikkDvhRepository
import no.nav.familie.ba.statistikk.integrasjoner.BasakClient
import no.nav.familie.ba.statistikk.integrasjoner.KafkaProducer
import no.nav.familie.ba.statistikk.integrasjoner.SaksstatistikkMellomlagringType
import no.nav.familie.ba.statistikk.integrasjoner.SaksstatistikkSendtRequest
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController()
@RequestMapping("/api/statistikk")
@ProtectedWithClaims(issuer = "azuread")
class SaksstatistikkController(val saksstatistikkDvhRepository: SaksstatistikkDvhRepository,
                               val basakClient: BasakClient,
                               val kafkaProducer: KafkaProducer
) {

    @GetMapping(path = ["/sak/offsett/{offset}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun sak(
        @PathVariable offset: Long,
        @RequestParam(required = false) fraAiven: Boolean = true
    ): String {
        return try {
            saksstatistikkDvhRepository.hent(if (fraAiven) SAK_2 else SAK, offset)
        } catch (e: EmptyResultDataAccessException) {
            "Fant ikke sak med offset $offset"
        }
    }


    @GetMapping(path = ["/sak/fagsakid/{fagsakId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun harSendtMeldingForFagsak(@PathVariable fagsakId: String): Ressurs<Boolean> {
        return Ressurs.success(saksstatistikkDvhRepository.antallFagsakMedId(fagsakId) > 0)
    }

    @GetMapping(path = ["/sak/fagsak/funksjonellid/{funksjonellId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun sakMedFunksjonellId(@PathVariable funksjonellId: String): Ressurs<List<String>> {
        return Ressurs.success(saksstatistikkDvhRepository.hentJSONForFagsakForFunksjonellId(funksjonellId))
    }

    @GetMapping(path = ["/sak/fagsak/id/{fagsakId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun sakMedBehandlingId(@PathVariable fagsakId: String): Ressurs<List<String>> {
        return Ressurs.success(saksstatistikkDvhRepository.hentJSONForFagsakForFagsakId(fagsakId))
    }


    @GetMapping(path = ["/behandling/offset/{offset}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun behandling(
        @PathVariable offset: Long,
        @RequestParam(required = false) fraAiven: Boolean = true
    ): String {
        return try {
            saksstatistikkDvhRepository.hent(if (fraAiven) BEHANDLING_2 else BEHANDLING, offset)
        } catch (e: EmptyResultDataAccessException) {
            "Fant ikke behandling med offset $offset"
        }
    }

    @GetMapping(path = ["/behandling/behandlingid/{behandlingId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun harSendtMeldingForBehandling(@PathVariable behandlingId: String): Ressurs<Boolean> {
        return Ressurs.success(saksstatistikkDvhRepository.antallBehandlingerMedId(behandlingId) > 0)
    }
    @GetMapping(path = ["/behandling/behandling/funksjonellid/{funksjonellId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun behandlingMedFunksjonellId(@PathVariable funksjonellId: String): Ressurs<List<String>> {
        return Ressurs.success(saksstatistikkDvhRepository.hentJSONBehandlingerMedFunksjonellId(funksjonellId))
    }

    @GetMapping(path = ["/behandling/behandling/id/{behandlingId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun behandlingMedBehandlingId(@PathVariable behandlingId: String): Ressurs<List<String>> {
        return Ressurs.success(saksstatistikkDvhRepository.hentJSONForBehandlingerMedBehandlingId(behandlingId))
    }

    @PostMapping(path = ["/behandling/patch/"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun patchBehandlingMelding(@RequestBody patchMelding: String): ResponseEntity<String> {
        val json = try {
            objectMapper.readTree(patchMelding)
        } catch (e: Exception) {
            error("Ikke gyldig json")
        }
        val fieldNames = json.fieldNames().asSequence().toList()

        try {
            if (!fieldNames.contains("funksjonellId")) error("Mangler funksjonellId på patchmelding")
            if (!fieldNames.contains("behandlingId")) error("Mangler behandlingId på patchmelding")
            if (!fieldNames.contains("versjon")) error("Mangler kontraktversjon på patchmelding")
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(e.message)
        }

        val funksjonellId = json.get("funksjonellId").asText()
        if (saksstatistikkDvhRepository.hentJSONBehandlingerMedFunksjonellId(funksjonellId).isEmpty()) {
            return ResponseEntity.badRequest().body("Finner ingen saksmelding med funksjonellId=$funksjonellId")
        }

        val offset = kafkaProducer.sendSakstatistikkBehandlingmelding(json.get("funksjonellId").asText(), patchMelding)
        val request =  SaksstatistikkSendtRequest(offset = offset,
        json = patchMelding,
        sendtTidspunkt = LocalDateTime.now(),
        type = SaksstatistikkMellomlagringType.BEHANDLING)
        return ResponseEntity.ok(basakClient.registrererSendtFraStatistikk(request))
    }

    @PostMapping(path = ["/sak/patch/"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun patchSakMelding(@RequestBody patchMelding: String): ResponseEntity<String> {
        val json = try {
            objectMapper.readTree(patchMelding)
        } catch (e: Exception) {
            error("Ikke gyldig json")
        }
        val fieldNames = json.fieldNames().asSequence().toList()

        try {
            if (!fieldNames.contains("funksjonellId")) error("Mangler funksjonellId på patchmelding")
            if (!fieldNames.contains("sakId")) error("Mangler sakId på patchmelding")
            if (!fieldNames.contains("versjon")) error("Mangler kontraktversjon på patchmelding")

        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(e.message)
        }

        val funksjonellId = json.get("funksjonellId").asText()
        if (saksstatistikkDvhRepository.hentJSONForFagsakForFunksjonellId(funksjonellId).isEmpty()) {
            return ResponseEntity.badRequest().body("Finner ingen saksmelding med funksjonellId=$funksjonellId")
        }

        val offset = kafkaProducer.sendSakstatistikkSakmelding(json.get("funksjonellId").asText(), patchMelding)
        val request =  SaksstatistikkSendtRequest(offset = offset,
                                                  json = patchMelding,
                                                  sendtTidspunkt = LocalDateTime.now(),
                                                  type = SaksstatistikkMellomlagringType.SAK)
        return ResponseEntity.ok(basakClient.registrererSendtFraStatistikk(request))
    }
}
