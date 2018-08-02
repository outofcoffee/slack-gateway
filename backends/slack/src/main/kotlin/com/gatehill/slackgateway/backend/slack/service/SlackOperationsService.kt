package com.gatehill.slackgateway.backend.slack.service

import com.fasterxml.jackson.module.kotlin.readValue
import com.gatehill.slackgateway.backend.slack.config.SlackSettings
import com.gatehill.slackgateway.backend.slack.model.GroupsCreateResponse
import com.gatehill.slackgateway.backend.slack.model.GroupsListResponse
import com.gatehill.slackgateway.backend.slack.model.SlackGroup
import com.gatehill.slackgateway.backend.slack.model.SlackUser
import com.gatehill.slackgateway.backend.slack.model.SlackUserGroup
import com.gatehill.slackgateway.backend.slack.model.UserGroupsListResponse
import com.gatehill.slackgateway.backend.slack.model.UserGroupsUsersListResponse
import com.gatehill.slackgateway.backend.slack.model.UsersListResponse
import com.gatehill.slackgateway.util.jsonMapper
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Common Slack operations.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SlackOperationsService @Inject constructor(private val slackApiService: SlackApiService) {
    private val logger: Logger = LogManager.getLogger(SlackOperationsService::class.java)

    private val cache = CacheBuilder.newBuilder()
        .expireAfterWrite(SlackSettings.cacheSeconds, TimeUnit.SECONDS)
        .build(object : CacheLoader<String, List<*>>() {
            override fun load(key: String) = when (key) {
                "users" -> fetchUsers()
                "userGroups" -> fetchUserGroups()
                else -> throw NotImplementedError()
            }
        })

    @Suppress("UNCHECKED_CAST")
    internal val users
        get() = cache["users"] as List<SlackUser>

    @Suppress("UNCHECKED_CAST")
    private val userGroups
        get() = cache["userGroups"] as List<SlackUserGroup>

    init {
        // force evaluation of token on startup
        SlackSettings.slackUserToken
    }

    private fun fetchUsers(): List<SlackUser> {
        logger.debug("Fetching all users")

        val reply = slackApiService.invokeSlackCommand<UsersListResponse>(commandName = "users.list")
        slackApiService.checkReplyOk(reply.ok)
        return reply.members
    }

    private fun fetchUserGroups(): List<SlackUserGroup> {
        logger.debug("Fetching all user groups")

        val reply = slackApiService.invokeSlackCommand<UserGroupsListResponse>(
            commandName = "usergroups.list",
            method = SlackApiService.HttpMethod.GET
        )

        slackApiService.checkReplyOk(reply.ok)
        return reply.usergroups
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
            )
        )

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
            )
        )

        slackApiService.checkReplyOk(reply)
    }

    /**
     * @return the user group with the given handle, or `null`
     */
    internal fun fetchUserGroup(userGroupHandle: String): SlackUserGroup? {
        logger.debug("Fetching user group: $userGroupHandle")
        return userGroups.firstOrNull { it.handle.equals(userGroupHandle, ignoreCase = true) }
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
            bodyMode = SlackApiService.BodyMode.JSON
        )

        slackApiService.checkReplyOk(reply)
    }
}
