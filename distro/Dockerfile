FROM eclipse-temurin:8u322-b06-jre-focal

LABEL MAINTAINER="Pete Cornish <outofcoffee@gmail.com>"

RUN mkdir -p /opt/slack-gateway

ADD build/install/distro /opt/slack-gateway

RUN ln -s /opt/slack-gateway/bin/distro /usr/local/bin/slack-gateway && \
    chmod +x /usr/local/bin/slack-gateway

EXPOSE 8080

WORKDIR /opt/slack-gateway

ENTRYPOINT [ "slack-gateway" ]
