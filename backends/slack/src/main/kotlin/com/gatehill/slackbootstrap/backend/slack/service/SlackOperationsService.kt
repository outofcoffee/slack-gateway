package com.gatehill.slackbootstrap.backend.slack.service

import com.fasterxml.jackson.module.kotlin.readValue
import com.gatehill.slackbootstrap.backend.slack.config.SlackSettings
import com.gatehill.slackbootstrap.backend.slack.model.*
import com.gatehill.slackbootstrap.util.jsonMapper
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.inject.Inject

/**
 * Common Slack operations.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SlackOperationsService @Inject constructor(private val slackApiService: SlackApiService) {
    private val logger: Logger = LogManager.getLogger(SlackOperationsService::class.java)

    internal val users by lazy {
        val reply = slackApiService.invokeSlackCommand<UsersListResponse>(commandName = "users.list")
        slackApiService.checkReplyOk(reply.ok)
        reply.members
    }

    init {
        // force evaluation of token on startup
        SlackSettings.slackUserToken
    }

    internal fun listPrivateChannels(): List<SlackGroup> {
        logger.debug("Listing private channels")

        val reply = slackApiService.invokeSlackCommand<GroupsListResponse>(commandName = "groups.list")
        slackApiService.checkReplyOk(reply.ok)
        return reply.groups
    }

    internal fun createPrivateChannel(channelName: String): SlackGroup {
        logger.debug("Creating channel: $channelName")

        val reply = slackApiService.invokeSlackCommand<GroupsCreateResponse>(
                commandName = "groups.create",
                params = mapOf(
                        "name" to channelName,
                        "validate" to "true"
                ))

        logger.debug("Create channel response: $reply")

        slackApiService.checkReplyOk(reply.ok)
        logger.debug("Channel $channelName created")
        return reply.group
    }

    internal fun inviteToPrivateChannel(channel: SlackGroup, memberId: String) {
        val reply = slackApiService.invokeSlackCommand<Map<String, Any>>(
                commandName = "groups.invite",
                params = mapOf(
                        "channel" to channel.id,
                        "user" to memberId
                ))

        slackApiService.checkReplyOk(reply)
    }

    /**
     * @return the user group with the given name, or `null`
     */
    internal fun fetchUserGroup(userGroupName: String): SlackUserGroup? {
        logger.debug("Fetching user group: $userGroupName")

        val reply = slackApiService.invokeSlackCommand<UserGroupsListResponse>(
                commandName = "usergroups.list",
                method = SlackApiService.HttpMethod.GET
        )

        slackApiService.checkReplyOk(reply.ok)
        return reply.usergroups.first { it.name.equals(userGroupName, ignoreCase = true) }
    }

    /**
     * @return the list of user IDs within the user group
     */
    internal fun listUserGroupUserIds(userGroup: SlackUserGroup): List<String> {
        logger.debug("Listing user IDs in user group: ${userGroup.name}")

        val reply = slackApiService.invokeSlackCommand<UserGroupsUsersListResponse>(
                commandName = "usergroups.users.list",
                method = SlackApiService.HttpMethod.GET,
                params = mapOf(
                        "usergroup" to userGroup.id
                )
        )

        slackApiService.checkReplyOk(reply.ok)
        return reply.users
    }

    internal fun sendMessage(channelName: String, message: String) {
        val params = jsonMapper.readValue<Map<String, *>>(message).toMutableMap()
        params += "channel" to channelName

        logger.info("Forwarding message to channel '$channelName': $message")

        val reply = slackApiService.invokeSlackCommand<Map<String, Any>>(
                commandName = "chat.postMessage",
                params = params,
                bodyMode = SlackApiService.BodyMode.JSON)

        slackApiService.checkReplyOk(reply)
    }
}
