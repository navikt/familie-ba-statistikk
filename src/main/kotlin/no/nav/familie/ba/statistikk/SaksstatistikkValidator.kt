package no.nav.familie.ba.statistikk

import com.fasterxml.jackson.core.JsonFactory
import com.worldturner.medeia.api.UrlSchemaSource
import com.worldturner.medeia.api.ValidationFailedException
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi
import io.micrometer.core.instrument.Metrics
import org.slf4j.LoggerFactory
import java.nio.charset.Charset

private val logger = LoggerFactory.getLogger(object {}::class.java)
private val secureLogger = LoggerFactory.getLogger("secureLogger")
private val sakValideringFeilCounter = Metrics.counter("barnetrygd.saksstatistikk.valideringsfeil.sak")
private val behandlingValideringFeilCounter = Metrics.counter("barnetrygd.saksstatistikk.valideringsfeil.behandling")
fun validerBehandlingDvhMotJsonSchema(json: String) {
    val api = MedeiaJacksonApi()
    val behandlingSchemaValidator = api.loadSchema(UrlSchemaSource(
            object {}::class.java.getResource("/schema/behandling-schema.json")))
    val validatedParser = api.decorateJsonParser(behandlingSchemaValidator,
                                                 JsonFactory().createParser(json.toByteArray(Charset.defaultCharset())))
    try {
        api.parseAll(validatedParser)
    } catch (e: ValidationFailedException) {
        behandlingValideringFeilCounter.increment()
        logger.error("json for behandling validerer ikke etter skjema", e)
        secureLogger.error("json for behandling validerer ikke etter skjema \n$json")
    }
}

fun validerSakDvhMotJsonSchema(json: String) {
    val api = MedeiaJacksonApi()
    val sakSchemaValidator = api.loadSchema(UrlSchemaSource(
            object {}::class.java.getResource("/schema/sak-schema.json")))
    val validatedParser = api.decorateJsonParser(sakSchemaValidator,
                                                 JsonFactory().createParser(json.toByteArray(Charset.defaultCharset())))
    try {
        api.parseAll(validatedParser)
    } catch (e: ValidationFailedException) {
        sakValideringFeilCounter.increment()
        logger.error("json for sak validerer ikke etter skjema", e)
        secureLogger.error("json for sak validerer ikke etter skjema \n$json")
    }
}