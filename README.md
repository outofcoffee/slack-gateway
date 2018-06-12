# Slack Bootstrap: A tool to create Slack channels and post to them [![CircleCI](https://circleci.com/gh/outofcoffee/slack-bootstrap.svg?style=svg)](https://circleci.com/gh/outofcoffee/slack-bootstrap)

## What can it do?

* Create a Slack channel if it doesn't exist
* Invite a list of users to the channel
* Post a given message to the channel

## Instructions

* As a Slack admin, create a Slack app, add the required scopes (see below) and install it to your workspace
* Set environment variables
* Run!

## Getting started

If you'd like to run Slack Bootstrap yourself as a Docker container, you can do the following:

    docker run -d \
        --env SLACK_USER_TOKEN="CHANGEME" \
        --env SLACK_CHANNEL_MEMBERS="jsmith,mjones" \
        --publish 8080:8080 \
        outofcoffee/slack-bootstrap

> Note: See the _Environment variables_ section for the available configuration settings.

## Example usage

Send a message to a channel:

    curl http://localhost:8080/post?channel=foo \
        --header "Content-Type: application/json" \
        --data '{"text":"Hello World!"}'
        
Send a message with attachments:

    curl http://localhost:8080/post?channel=foo \
        --header "Content-Type: application/json" \
        --data '{"text":"Hello World!","attachments":[{"text":"More info","color":"#33ee33"}]}'

## Creating a Slack app

As a Slack admin, create a Slack app: https://api.slack.com/apps/new

Add the required scopes:

    https://api.slack.com/apps/<your app ID>/oauth

The scopes are:

    users:read
    groups:read
    groups:write
    chat:write:bot
    
> Don't forget to save changes after adding scopes.

Install your app to your workspace. This will generate the token you need. You'll want to copy the 'OAuth Access Token'. It should look like this:

    xoxp-123456789012-123456789012-123456789012-abcdef1234567890abcdef1234567890

## Build

If instead you wish to build and run locally, you can run:

    ./gradlew installDist
    docker-compose build

Once built, set the environment variables in `docker-compose.yml`. See the _Environment variables_ section.

Then run with:

    docker-compose up

If you change anything, don't forget to rebuild before running again.

## Environment variables

Configure the bot using the following environment variables.

- SLACK_USER_TOKEN - must have the right permission scopes (see 'Creating a Slack app' in this document)
- SLACK_CHANNEL_MEMBERS - users to invite e.g. "janesmith,bob"
- HTTP_BIND_PORT (default 8080)
- HTTP_BIND_HOST (default 0.0.0.0)

## More info

Slack API: https://api.slack.com/bot-users

## Contributing

* Pull requests are welcome.
* PRs should target the `develop` branch.
* Please run `ktlint` against the code first ;-)

## Author

Pete Cornish (outofcoffee@gmail.com)
