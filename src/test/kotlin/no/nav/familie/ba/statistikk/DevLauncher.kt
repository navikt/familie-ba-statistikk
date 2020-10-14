package no.nav.familie.ba.statistikk

import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication(scanBasePackages = ["no.nav.familie.ba.statistikk"])
class DevLauncher {


    companion object {
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
        private val logger = LoggerFactory.getLogger(this::class.java)
        fun log() {
            logger.info("familie-ba-statistikk starter opp")
            secureLogger.info("familie-ba-statistikk starter opp")
        }
    }

}


fun main(args: Array<String>) {
    DevLauncher.log()
    val springApp = SpringApplication(DevLauncher::class.java)
    springApp.setAdditionalProfiles("dev")
    springApp.run(*args)
}