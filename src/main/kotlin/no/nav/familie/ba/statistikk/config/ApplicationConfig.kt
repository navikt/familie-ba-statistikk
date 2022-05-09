package no.nav.familie.ba.statistikk.config

import no.nav.familie.log.filter.LogFilter
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootConfiguration
@ConfigurationPropertiesScan
@ComponentScan(ApplicationConfig.pakkenavn)
@EnableScheduling
@EnableJwtTokenValidation(ignore = ["org.springframework","org.springdoc"])
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
        const val pakkenavn = "no.nav.familie.ba.statistikk"
    }
}
