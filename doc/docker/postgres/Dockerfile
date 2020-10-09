FROM postgis/postgis:12-master

COPY .vimrc /root/.vimrc
COPY bashrc /color-config

RUN \
    apt-get update && \
    apt-get install -y vim && \
    cat color-config >> /etc/bash.bashrc




