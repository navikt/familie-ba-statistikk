package no.nav.familie.ba.vedtak.consumer

import org.slf4j.LoggerFactory
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@SpringBootConfiguration
@EntityScan("no.nav.familie.ba.vedtak.consumer", ApplicationConfig.pakkenavn)
@ComponentScan("no.nav.familie.ba.vedtak.consumer", ApplicationConfig.pakkenavn)
@ConfigurationPropertiesScan
class ApplicationConfig {

    companion object {
        private val log = LoggerFactory.getLogger(ApplicationConfig::class.java)
        const val pakkenavn = "no.nav.familie.ba.vedtak.consumer"
    }
}
