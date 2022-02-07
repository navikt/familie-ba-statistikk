package no.nav.familie.ba.statistikk.rest

import no.nav.familie.ba.statistikk.domene.VedtakDvhRepository
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController()
@RequestMapping("/api/vedtak")
@ProtectedWithClaims(issuer = "azuread")
class VedtakController(val vedtakDvhRepository: VedtakDvhRepository) {

    @GetMapping(path = ["/{behandlingId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun harSendtVedtaksmeldingForBehandling(@PathVariable behandlingId: String) : Ressurs<Boolean> {
        return Ressurs.success(vedtakDvhRepository.finnes(behandlingId))
    }
}