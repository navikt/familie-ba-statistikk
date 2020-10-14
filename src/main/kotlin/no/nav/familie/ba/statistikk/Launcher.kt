package no.nav.familie.ba.statistikk

import no.nav.familie.ba.statistikk.config.ApplicationConfig
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(scanBasePackages = ["no.nav.familie.ba.statistikk"])
class Launcher {
    companion object {
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
        private val logger = LoggerFactory.getLogger(this::class.java)
        fun log() {
            logger.info("familie-ba-statistikk startet opp")
            secureLogger.info("familie-ba-statistikk startet opp")
        }

    }

}

fun main(args: Array<String>) {
    val app = SpringApplication(ApplicationConfig::class.java)
    app.run(*args)
    Launcher.log()
}