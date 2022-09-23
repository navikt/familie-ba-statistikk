# familie-ba-statistikk

Konsumerer meldinger fra ba-sak for stønad og saksstatistikk og lagrer de ned i en tabell

## tabeller
saksstatistikk_dvh - Meldinger relatert til saksstatistikk - 2 typer. BEHANDLING og SAK
vedtak_dvh - meldinger for stønadsstatistikk

## nyttige spørringer

### identifisere manglende meldinger
```
SELECT s.id AS missing_ids
FROM generate_series(0, 1427044) s(id)
WHERE NOT EXISTS (SELECT 1 FROM saksstatistikk_dvh WHERE type ='BEHANDLING' AND offset_verdi = s.id);

SELECT s.id AS missing_ids
FROM generate_series(0, 31010) s(id)
WHERE NOT EXISTS (SELECT 1 FROM vedtak_dvh WHERE type ='VEDTAK_V2' AND offset_verdi = s.id);
```

### Finne meldinger med kompetanseperioder med sokersaktivitet satt en verdi
```
SELECT v.vedtak_json -> 'kompetanseperioder'
FROM vedtak_dvh v
         CROSS JOIN jsonb_array_elements(v.vedtak_json -> 'kompetanseperioder')
where v.type = 'VEDTAK_V2' AND value->>'sokersaktivitet' IN ('MOTTAR_PENSJON_FRA_NORGE');
```

## historisk info
Det ble gjort konvertering og resending av alle saksstatistikkmeldinger. Fram til dette så hadde det blitt sendt en del meldinger på behandlinger/saker som hadde blitt rullet tilbake i ba-sak. Før rekjøring ble topicene tømt for innhold. Første melding i topic vil da være:  
offset SAK: 4722  
offset BEHANDLING: 11958

### Oppsummering av resending
kontraktversjon: 2.0_20200930143114_268ea08  
offset opp til 2645. Alle disse ble ignorert siden dette var meldinger på behandlinger var rullet tilbake i databasen til ba-sak og aldri skulle vært sendt

kontraktversjon: 2.0_20201012132018_dc05978  
1004 behandlingId total. Alle unntatt 25 ble ignorert siden de ikke eksisterte i databasen til ba-sak, fordi de aldri skulle vært sendt

kontraktversjon: 2.0_20201110145948_2c83b39  
Meldingene fikk funksjonellId

kontraktversjon: 2.0_20201217113247_876a253  
-ingen endring i formatet

kontraktversjon: 2.0_20210128104331_9bd0bd2  
Resendt med nye felt:  
- behandlingsÅrsak
- automatiskBehandlet
- behandlingUnderkategori

kontraktversjon: 2.0_20210211132003_f81111d  
resultatBegrunnelser lagt til kontrakt  
5330 meldinger  
offset: 5156 - 10485  
321 behandlinger ble konvertert  


kontraktversjon: 2.0_20210427132344_d9066f5  
utvidelser av vedtaksbegrunnelse-lengde  
1472 meldinger, 75 behandlinger ble konvertert  
offset: 10486-11957  

