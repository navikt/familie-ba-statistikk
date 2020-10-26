FROM navikt/java:11
RUN apt-get -yy install kafkacat

COPY ./target/familie-ba-statistikk.jar "app.jar"
COPY init.sh /init-scripts/init.sh

RUN chmod +x /init-scripts/init.sh
