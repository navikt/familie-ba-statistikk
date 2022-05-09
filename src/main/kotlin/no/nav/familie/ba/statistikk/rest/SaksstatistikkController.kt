package no.nav.familie.ba.statistikk.rest

import no.nav.familie.ba.statistikk.domene.SaksstatistikkDvhRepository
import no.nav.familie.eksterne.kontrakter.saksstatistikk.BehandlingDVH
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("/api/statistikk")
@ProtectedWithClaims(issuer = "azuread")
class SaksstatistikkController(val saksstatistikkDvhRepository: SaksstatistikkDvhRepository) {

    @GetMapping(path = ["/sak/offsett/{offset}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun sak(@PathVariable offset: Long): String {
        return try {
            saksstatistikkDvhRepository.hent("SAK", offset)
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
    fun behandling(@PathVariable offset: Long): String {
        return try {
            saksstatistikkDvhRepository.hent("BEHANDLING", offset)
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
}
