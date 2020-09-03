package no.nav.familie.ba.statistikk

import no.nav.familie.ba.statistikk.config.ApplicationConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(scanBasePackages = ["no.nav.familie.ba.statistikk"])
class Launcher

fun main(args: Array<String>) {
    val app = SpringApplication(ApplicationConfig::class.java)
    app.run(*args)
}