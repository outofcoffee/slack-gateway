package com.gatehill.slackbootstrap.backend.slack.service

import com.fasterxml.jackson.module.kotlin.readValue
import com.gatehill.slackbootstrap.backend.slack.config.SlackSettings
import com.gatehill.slackbootstrap.backend.slack.model.*
import com.gatehill.slackbootstrap.service.OutboundMessageService
import com.gatehill.slackbootstrap.util.jsonMapper
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.inject.Inject

/**
 * Allows an item to be locked or unlocked by a user.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SlackOutboundMessageService @Inject constructor(private val slackApiService: SlackApiService) : OutboundMessageService {
    private val logger: Logger = LogManager.getLogger(SlackOutboundMessageService::class.java)

    private val users by lazy {
        val reply = slackApiService.invokeSlackCommand<UsersListResponse>(commandName = "users.list")
        slackApiService.checkReplyOk(reply.ok)
        reply.members
    }

    init {
        // force evaluation of token on startup
        SlackSettings.slackUserToken
    }

    override fun forward(channelName: String, message: String) {
        val channel = ensureChannelExists(channelName)
        checkParticipants(channel)
        sendMessage(channelName, message)
    }

    private fun ensureChannelExists(channelName: String): SlackGroup {
        val reply = slackApiService.invokeSlackCommand<GroupsListResponse>(commandName = "groups.list")
        slackApiService.checkReplyOk(reply.ok)

        reply.groups.firstOrNull { it.name == channelName }?.let {
            // channel already exists
            logger.debug("Channel $channelName already exists")
            return it

        } ?: run {
            // create the channel
            logger.debug("Channel $channelName does not exist - creating")
            return createChannel(channelName)
        }
    }

    private fun createChannel(channelName: String): SlackGroup {
        val reply = slackApiService.invokeSlackCommand<GroupsCreateResponse>(
                commandName = "groups.create",
                params = mapOf(
                        "name" to channelName,
                        "validate" to "true"
                ))

        logger.debug("Create channel response: $reply")

        try {
            slackApiService.checkReplyOk(reply.ok)
            logger.debug("Channel $channelName created")
            return reply.group

        } catch (e: Exception) {
            throw RuntimeException("Error parsing channel creation response", e)
        }
    }

    private fun checkParticipants(channel: SlackGroup) {
        logger.debug("Checking participants of channel: ${channel.name}")

        val memberIds = mutableListOf<String>()

        SlackSettings.inviteGroups.forEach { userGroupName ->
            try {
                fetchUserGroup(userGroupName)?.let { userGroup ->
                    memberIds += listUserGroupUserIds(userGroup)
                }
            } catch (e: Exception) {
                logger.warn("Error inviting user group: $userGroupName - continuing", e)
            }
        }

        memberIds += SlackSettings.inviteMembers.mapNotNull { memberUsername ->
            users.firstOrNull { user -> user.name == memberUsername }?.id
        }

        logger.info("Inviting ${memberIds.size} members to channel: ${channel.name}")

        memberIds
                .filterNot { memberId: String -> channel.members.contains(memberId) }
                .forEach { memberId ->
                    logger.info("Inviting member $memberId to channel ${channel.name}")
                    try {
                        inviteToChannel(channel, memberId)
                    } catch (e: Exception) {
                        logger.warn("Error inviting member $memberId to channel ${channel.name} - continuing", e)
                    }
                }
    }

    private fun inviteToChannel(channel: SlackGroup, memberId: String) {
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
    private fun fetchUserGroup(userGroupName: String): SlackUserGroup? {
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
    private fun listUserGroupUserIds(userGroup: SlackUserGroup): List<String> {
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

    private fun sendMessage(channelName: String, message: String) {
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
