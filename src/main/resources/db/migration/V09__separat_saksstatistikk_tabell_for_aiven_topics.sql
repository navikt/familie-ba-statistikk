ALTER TABLE saksstatistikk_dvh
RENAME TO saksstatistikk_dvh_onprem;

CREATE TABLE SAKSSTATISTIKK_DVH
(
    ID            BIGINT                              NOT NULL PRIMARY KEY,
    OFFSET_VERDI  BIGINT                              NOT NULL,
    TYPE          VARCHAR                             NOT NULL,
    JSON          jsonb                               NOT NULL,
    OPPRETTET_TID TIMESTAMP(3) DEFAULT LOCALTIMESTAMP NOT NULL,
    ER_DUPLIKAT   BOOLEAN      DEFAULT FALSE,
    FUNKSJONELL_ID VARCHAR                            NOT NULL
);

CREATE INDEX IF NOT EXISTS sak_funksjonellid_idx ON saksstatistikk_dvh(funksjonell_id);