package no.nav.familie.ba.statistikk

import no.nav.familie.eksterne.kontrakter.AnnenForeldersAktivitet
import no.nav.familie.eksterne.kontrakter.BehandlingTypeV2
import no.nav.familie.eksterne.kontrakter.BehandlingÅrsakV2
import no.nav.familie.eksterne.kontrakter.KategoriV2
import no.nav.familie.eksterne.kontrakter.Kompetanse
import no.nav.familie.eksterne.kontrakter.KompetanseAktivitet
import no.nav.familie.eksterne.kontrakter.KompetanseResultat
import no.nav.familie.eksterne.kontrakter.PersonDVHV2
import no.nav.familie.eksterne.kontrakter.SøkersAktivitet
import no.nav.familie.eksterne.kontrakter.UnderkategoriV2
import no.nav.familie.eksterne.kontrakter.UtbetalingsperiodeDVHV2
import no.nav.familie.eksterne.kontrakter.VedtakDVHV2
import no.nav.familie.eksterne.kontrakter.saksstatistikk.BehandlingDVH
import no.nav.familie.eksterne.kontrakter.saksstatistikk.ResultatBegrunnelseDVH
import no.nav.familie.eksterne.kontrakter.saksstatistikk.SakDVH
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime

object TestData {

    fun vedtakDvhV2(): VedtakDVHV2 {
        return VedtakDVHV2(
            fagsakId = "1",
            behandlingsId = "1",
            funksjonellId = "funksjonellId",
            tidspunktVedtak = ZonedDateTime.now(),
            personV2 = PersonDVHV2("123", "rolle", listOf(), "NOR", 0),
            ensligForsørger = false,
            kategoriV2 = KategoriV2.NASJONAL,
            underkategoriV2 = UnderkategoriV2.ORDINÆR,
            behandlingTypeV2 = BehandlingTypeV2.FØRSTEGANGSBEHANDLING,
            behandlingÅrsakV2 = BehandlingÅrsakV2.SØKNAD,
            utbetalingsperioderV2 = listOf(
                UtbetalingsperiodeDVHV2(
                    hjemmel = "hjemmel",
                    utbetaltPerMnd = 1054,
                    stønadFom = LocalDate.now(),
                    stønadTom = LocalDate.now().plusYears(17),
                    utbetalingsDetaljer = listOf()
                )
            ),
            kompetanseperioder = listOf(
                Kompetanse(
                    barnsIdenter = listOf("1", "2"),
                    barnetsBostedsland = "NO",
                    annenForeldersAktivitet = KompetanseAktivitet.FORSIKRET_I_BOSTEDSLAND,
                    annenForeldersAktivitetsland = "GB",
                    sokersaktivitet = KompetanseAktivitet.ARBEIDER,
                    resultat = KompetanseResultat.NORGE_ER_PRIMÆRLAND,
                    fom = YearMonth.now(),
                    tom = YearMonth.now()
                )
            )
        )
    }

    fun sakDvh(): SakDVH {
        return SakDVH(
            funksjonellTid = ZonedDateTime.now(),
            tekniskTid = ZonedDateTime.now(),
            opprettetDato = LocalDate.now(),
            funksjonellId = "funksjonellId",
            sakId = "1",
            aktorId = 1234567891011,
            sakStatus = "OPPRETTET",
            avsender = "VL",
            versjon = "1",
            bostedsland = "NOR"
        )
    }

    fun behandlingDvh(): BehandlingDVH {
        return BehandlingDVH(
            funksjonellTid = ZonedDateTime.now(),
            tekniskTid = ZonedDateTime.now(),
            mottattDato = ZonedDateTime.now(),
            registrertDato = ZonedDateTime.now(),
            behandlingId = "behandling.id.toString()",
            funksjonellId = "funksjonellId",
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
            behandlingUnderkategori = "ORDINÆR",
            behandlingAarsak = "SØKNAD",
            automatiskBehandlet = false,
            resultatBegrunnelser = listOf(
                ResultatBegrunnelseDVH(
                    LocalDate.now(),
                    LocalDate.now().plusYears(1),
                    "INNVILGELSE",
                    "INNVILGET_BOSTATT_I_RIKET"
                )
            )
        )
    }
}