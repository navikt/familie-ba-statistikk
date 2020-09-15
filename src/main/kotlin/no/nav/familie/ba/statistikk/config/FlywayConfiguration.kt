package no.nav.familie.ba.statistikk.config

import org.flywaydb.core.api.configuration.FluentConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@ConditionalOnProperty("spring.flyway.enabled")
class FlywayConfiguration {

    @Bean
    fun flywayConfig(@Value("\${DB_ROLE}") role: String): FlywayConfigurationCustomizer {
        return FlywayConfigurationCustomizer { c: FluentConfiguration ->
            c.initSql("SET ROLE \"$role\"")
        }
    }
}