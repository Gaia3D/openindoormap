FROM tomcat:9.0.38-jdk11-adoptopenjdk-openj9

RUN \
    apt-get update && \
    apt-get install -y vim wget unzip && \
    rm -rf /usr/local/tomcat/webapps/* && \
    wget -o /tmp/geoserver.zip http://sourceforge.net/projects/geoserver/files/GeoServer/2.17.3/geoserver-2.17.3-war.zip && \
    unzip /tmp/geoserver.zip -D /usr/local/tomcat/webapps && \
    rm -rf /tmp/geoserver.zip

CMD ["catalina.sh","run"]



