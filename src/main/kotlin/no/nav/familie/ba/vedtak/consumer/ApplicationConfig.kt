package no.nav.familie.ba.vedtak.consumer

import no.nav.familie.log.filter.LogFilter
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

    @Bean
    fun logFilter(): FilterRegistrationBean<LogFilter> {
        log.info("Registering LogFilter filter")
        val filterRegistration: FilterRegistrationBean<LogFilter> = FilterRegistrationBean()
        filterRegistration.filter = LogFilter()
        filterRegistration.order = 1
        return filterRegistration
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
        const val pakkenavn = "no.nav.familie.ba.vedtak.consumer"
    }
}
