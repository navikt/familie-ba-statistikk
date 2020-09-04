CREATE TABLE VEDTAK_DVH (
                      ID            bigint                                               NOT NULL PRIMARY KEY,
                      VEDTAK_JSON   jsonb                                                NOT NULL,
                      OPPRETTET_TID timestamp(3) DEFAULT LOCALTIMESTAMP                  NOT NULL
);

CREATE SEQUENCE VEDTAK_DVH_SEQ INCREMENT BY 50 START WITH 1000000 NO CYCLE;