FROM ghcr.io/navikt/baseimages/temurin:21

COPY ./target/familie-ba-statistikk.jar "app.jar"
COPY init.sh /init-scripts/init.sh
