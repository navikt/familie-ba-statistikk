package no.nav.familie.ba.statistikk.integrasjoner

import no.nav.familie.http.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI
import java.time.LocalDateTime

@Component
class BasakClient @Autowired constructor(
    @param:Value("\${BA_SAK_API_URL}") private val sakServiceUri: String,
    @Qualifier("azure") restOperations: RestOperations
) : AbstractRestClient(restOperations, "statistiskk.patch") {

    fun registrererSendtFraStatistikk(request: SaksstatistikkSendtRequest): String {
        val uri = URI.create("$sakServiceUri/saksstatistikk/registrer-sendt-fra-statistikk")
        val response: String = postForEntity(uri, request)
        return response
    }
}

data class SaksstatistikkSendtRequest(
    val offset: Long,
    val type: SaksstatistikkMellomlagringType,
    val json: String,
    val sendtTidspunkt: LocalDateTime
)
enum class SaksstatistikkMellomlagringType {
    SAK,
    BEHANDLING
}
