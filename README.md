# Slack Gateway: An HTTP gateway for posting to Slack [![CircleCI](https://circleci.com/gh/outofcoffee/slack-gateway.svg?style=svg)](https://circleci.com/gh/outofcoffee/slack-gateway)

With a single HTTP request, you can post a message, creating the channel first if it doesn't exist and invite people and groups.

## What can it do?

Runs as a server, listening for an HTTP request. Upon receipt of a message, the gateway:

* creates the given Slack channel if it doesn't exist
* ensures a list of users (or user groups) are in the channel (if not - invites them)
* posts the message to the channel

This removes the complexity of orchestrating many Slack API calls behind a simple endpoint.

## Instructions

* As a Slack admin, create a Slack app, add the required scopes (see below) and install it to your workspace
* Set environment variables
* Run!

## Getting started

If you'd like to run Slack Gateway yourself as a Docker container, you can do the following:

    docker run -d \
        --env SLACK_USER_TOKEN="CHANGEME" \
        --env SLACK_CHANNEL_MEMBERS="jsmith,mjones" \
        --publish 8080:8080 \
        outofcoffee/slack-gateway

> Note: See the _Environment variables_ section for the available configuration settings.

## Example usage

### Using Slack format

Send a message to a channel:

    curl http://localhost:8080/messages/raw \
        --header "Content-Type: application/json" \
        --data '{"channel":"general", text":"Hello World!"}'
        
Send a message with attachments:

    curl http://localhost:8080/messages/raw \
        --header "Content-Type: application/json" \
        --data '{"channel":"general", text":"Hello World!","attachments":[{"text":"More info","color":"#33ee33"}]}'

> For more information on the Slack post message format, see https://api.slack.com/

### Using simple key-value format

If you don't want to compose the JSON yourself, you can use a simpler, but slightly more limited, key-value format instead.

Send a message to a channel:

    curl http://localhost:8080/messages/text \
        --data 'channel=general' \
        --data 'text=Hello%20World!'

Send a message as an attachment:

    curl http://localhost:8080/messages/text \
        --data 'channel=general' \
        --data 'text=Hello%20World!' \
        --data 'title=Attachment' \
        --data 'attachment=true' \
        --data 'color=00ff00'

> The example above will send the text message as an attachment, with a particular colour and title.

The available options for the key-value format are:

| Key                     | Required                                 | Type    | Purpose                            |
|-------------------------|------------------------------------------|---------|------------------------------------|
| channel                 | Yes                                      | String  | Channel name                       |
| text                    | Yes, unless additional message keys used | String  | Literal message text               |
| attachment              | No                                       | Boolean | Whether to send in attachment mode |
| title                   | No                                       | String  | Attachment title                   |
| color                   | No                                       | String  | Attachment color                   |
| author_name             | No                                       | String  | Attachment author name             |
| title_link              | No                                       | String  | Attachment title link URL          |
| footer                  | No                                       | String  | Attachment footer text             |
| footer_icon             | No                                       | String  | Attachment footer icon URL         |
| Additional message keys | No, unless `text` is empty               | String  | See below                          |

#### Additional message keys

When using the key-value format, any additional keys are appended to the text message. For example:

    curl http://localhost:8080/messages/text \
        --data 'channel=general' \
        --data 'key_one=foo' \
        --data 'key_two=bar' \
        --data 'key_three=baz'

This will result in message text such as the following:

    key one: *foo* | key two: *bar* | key three: *baz*

> Note that underscores in key names are replaced with spaces, and the values are emboldened.

### Setting the channel type

You can specify the channel type as public or private. This will be used when creating channels, or posting messages.

Set the `channel_type` query parameter in the HTTP request, or use the `DEFAULT_CHANNEL_TYPE` environment variable.

Valid values:

* private
* public

Example using the query parameter:

    curl http://localhost:8080/messages/text \
       --data 'channel=some-private-channel' \
       --data 'text=Hello%20world' \
       --data 'channel_type=private'

## Creating a Slack app

As a Slack admin, create a Slack app: https://api.slack.com/apps/new

Add a bot user in the 'Bot Users' section:

    https://api.slack.com/apps/<your app ID>/bots

Add the required scopes in the 'OAuth & Permissions' section:

    https://api.slack.com/apps/<your app ID>/oauth

The scopes are:

    chat:write:bot
    channels:read
    channels:write
    groups:read
    groups:write
    users:read
    usergroups:read

> Don't forget to save changes after adding scopes.

Install your app to your workspace. This will generate the token you need. You'll want to copy the 'OAuth Access Token'. It should look like this:

    xoxp-123456789012-123456789012-123456789012-abcdef1234567890abcdef1234567890

Don't forget to invite your app to any existing private channels, using:

    /invite @YourAppName

> Slack doesn't permit apps to post to channels unless they have permissions.

---

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

### Basic variables

- SLACK_USER_TOKEN - must have the right permission scopes (see 'Creating a Slack app' in this document)
- SLACK_CHANNEL_MEMBERS - users to invite to channels e.g. "janesmith,bob" (comma separated; default empty)
- SLACK_CHANNEL_GROUPS - user groups to invite to channels e.g. "devteam" (comma separated; default empty)

### Advanced variables

- HTTP_BIND_PORT (default 8080)
- HTTP_BIND_HOST (default 0.0.0.0)
- SLACK_CACHE_SECONDS - period to cache Slack objects like users and user groups (default 300)
- DEFAULT_CHANNEL_TYPE (default 'private') - the default channel type if none is specified in the request

## Contributing

* Pull requests are welcome.
* PRs should target the `develop` branch.
* Please run `ktlint` against the code first ;-)

## Author

Pete Cornish (outofcoffee@gmail.com)
