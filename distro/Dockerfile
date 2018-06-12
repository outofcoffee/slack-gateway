FROM openjdk:8-jdk

LABEL MAINTAINER="Pete Cornish <outofcoffee@gmail.com>"

RUN mkdir -p /opt/slack-bootstrap

ADD build/install/distro /opt/slack-bootstrap

RUN ln -s /opt/slack-bootstrap/bin/distro /usr/local/bin/slack-bootstrap && \
    chmod +x /usr/local/bin/slack-bootstrap

EXPOSE 8080

WORKDIR /opt/slack-bootstrap

ENTRYPOINT [ "slack-bootstrap" ]
