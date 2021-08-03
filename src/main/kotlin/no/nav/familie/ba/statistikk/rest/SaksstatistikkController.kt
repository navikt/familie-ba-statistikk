package no.nav.familie.ba.statistikk.rest

import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController()
@RequestMapping("/api/statistikk")
@ProtectedWithClaims(issuer = "azuread")
class SaksstatistikkController {

    @GetMapping("/sak")
    fun sak() : String {
        return "OK"
    }
}