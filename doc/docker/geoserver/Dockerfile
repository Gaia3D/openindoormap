FROM tomcat:9.0.38-jdk11-adoptopenjdk-openj9

ENV JAVA_OPTS=-D-Xms4096m-Xmx4096m
ENV GEOSERVER_DATA_DIR=/geoserver-data

RUN \
    apt-get update && \
    apt-get install -y vim wget unzip && \
    rm -rf /usr/local/tomcat/webapps/* && \
    cd /tmp && wget http://sourceforge.net/projects/geoserver/files/GeoServer/2.17.3/geoserver-2.17.3-war.zip && \
    unzip /tmp/geoserver-2.17.3-war.zip -d /usr/local/tomcat/webapps && \
    rm -rf /tmp/geoserver-2.17.3-war.zip


