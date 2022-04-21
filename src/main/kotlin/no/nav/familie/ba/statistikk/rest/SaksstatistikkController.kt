package no.nav.familie.ba.statistikk.rest

import no.nav.familie.ba.statistikk.domene.SaksstatistikkDvhRepository
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController()
@RequestMapping("/api/statistikk")
@ProtectedWithClaims(issuer = "azuread")
class SaksstatistikkController(val saksstatistikkDvhRepository: SaksstatistikkDvhRepository) {

    @GetMapping(path = ["/sak/{offset}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun sak(@PathVariable offset: Long) : String {
        return saksstatistikkDvhRepository.hent("SAK", offset)
    }

    @GetMapping(path = ["/behandling/{offset}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun behandling(@PathVariable offset: Long): String {
        return saksstatistikkDvhRepository.hent("BEHANDLING", offset)
    }

    @GetMapping(path = ["/sak/fagsakid/{fagsakId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun harSendtMeldingForFagsak(@PathVariable fagsakId: String) : Ressurs<Boolean> {
        return Ressurs.success(saksstatistikkDvhRepository.antallFagsakMedId(fagsakId) > 0)
    }

    @GetMapping(path = ["/sak/behandlingid/{behandlingId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun harSendtMeldingForBehandling(@PathVariable behandlingId: String) : Ressurs<Boolean> {
        return Ressurs.success(saksstatistikkDvhRepository.antallBehandlingerMedId(behandlingId) > 0)
    }
}