package no.nav.familie.ba.statistikk

import no.nav.familie.eksterne.kontrakter.*
import no.nav.familie.eksterne.kontrakter.saksstatistikk.BehandlingDVH
import no.nav.familie.eksterne.kontrakter.saksstatistikk.SakDVH
import java.time.LocalDate
import java.time.ZonedDateTime

object TestData {

    fun vedtakDvh(): VedtakDVH {
        return VedtakDVH(fagsakId = "1",
                         behandlingsId = "1",
                         tidspunktVedtak = LocalDate.now(),
                         personIdent = "12345678910",
                         ensligForsørger = false,
                         kategori = Kategori.NASJONAL,
                         underkategori = Underkategori.ORDINÆR,
                         behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
                         behandlingOpprinnelse = BehandlingOpprinnelse.AUTOMATISK_VED_FØDSELSHENDELSE,
                         utbetalingsperioder = listOf(
                                 UtbetalingsperiodeDVH(hjemmel = "hjemmel",
                                                       utbetaltPerMnd = 1054,
                                                       stønadFom = LocalDate.now(),
                                                       stønadTom = LocalDate.now().plusYears(17),
                                                       utbetalingsDetaljer = listOf())))
    }

    fun sakDvh(): SakDVH {
        return SakDVH(funksjonellTid = ZonedDateTime.now(),
                      tekniskTid = ZonedDateTime.now(),
                      opprettetDato = LocalDate.now(),
                      sakId = "1",
                      aktorId = 1234567891011,
                      sakStatus = "OPPRETTET",
                      avsender = "VL",
                      versjon = "1"
        )
    }

    fun behandlingDvh(): BehandlingDVH {
        return BehandlingDVH(funksjonellTid = ZonedDateTime.now(),
                             tekniskTid = ZonedDateTime.now(),
                             mottattDato = ZonedDateTime.now(),
                             registrertDato = ZonedDateTime.now(),
                             behandlingId = "behandling.id.toString()",
                             sakId = "behandling.fagsak.id.toString()",
                             behandlingType = "behandling.type.name",
                             behandlingStatus = "behandling.status.name",
                             utenlandstilsnitt = "NASJONAL",
                             ansvarligEnhetKode = "ansvarligEnhetKode",
                             behandlendeEnhetKode = "behandlendeEnhetsKode",
                             ansvarligEnhetType = "NORG",
                             behandlendeEnhetType = "NORG",
                             totrinnsbehandling = true,
                             avsender = "familie-ba-sak",
                             versjon = "2",
                             behandlingKategori = "EØS",
                             behandlingUnderkategori = "ORDINÆR")
    }
}