package no.nav.familie.ba.statistikk.domene

import java.time.LocalDateTime
import javax.persistence.*

@Entity(name = "VedtakDvh")
@Table(name = "VEDTAK_DVH")
data class VedtakDvh(
        @Id
        @Column(name = "ID")
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VEDTAK_DVH_SEQ_GENERATOR")
        @SequenceGenerator(name = "VEDTAK_DVH_SEQ_GENERATOR", sequenceName = "VEDTAK_DVH_SEQ", allocationSize = 50)
        val id: Long = 0,

        @Column(name = "VEDTAK_JSON")
        val vedtakJson: String,

        @Column(name = "OPPRETTET_TID")
        val opprettetTid: LocalDateTime = LocalDateTime.now()
)