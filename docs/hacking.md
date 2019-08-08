# Local development

Set required environment variables in a file name `.env` next to the Docker Compose file.

    SLACK_USER_TOKEN=CHANGEME
    SLACK_CHANNEL_MEMBERS=jsmith,mjones

Build the distribution:

    ./gradlew clean installdist

Start the container locally:

     docker-compose up --build --force-recreate

Check it works:

    curl http://localhost:8080/messages/text \
            --data 'channel=bot-test' \
            --data 'text=Hello%20World!'

## Java debugger

Set the `JAVA_OPTS` environment variable:

    JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000
