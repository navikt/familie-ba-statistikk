package no.nav.familie.ba.statistikk

import no.nav.familie.eksterne.kontrakter.*
import java.time.LocalDate

object TestData {
    fun vedtakDhv(): VedtakDVH {
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
}