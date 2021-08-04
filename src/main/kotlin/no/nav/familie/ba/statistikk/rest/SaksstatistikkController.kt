package no.nav.familie.ba.statistikk.rest

import no.nav.familie.ba.statistikk.domene.SaksstatistikkDvhRepository
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController()
@RequestMapping("/api/statistikk")
@ProtectedWithClaims(issuer = "azuread")
class SaksstatistikkController(val saksstatistikkDvhRepository: SaksstatistikkDvhRepository) {

    @GetMapping("/sak/{offset}")
    fun sak(@PathVariable offset: Long) : String {
        return saksstatistikkDvhRepository.hent("SAK", offset)
    }

    @GetMapping("/behandling/{offset}")
    fun behandling(@PathVariable offset: Long): String {
        return saksstatistikkDvhRepository.hent("BEHANDLING", offset)
    }
}