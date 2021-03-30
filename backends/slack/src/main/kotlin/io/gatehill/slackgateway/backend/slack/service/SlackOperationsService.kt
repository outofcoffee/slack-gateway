package io.gatehill.slackgateway.backend.slack.service

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.gatehill.slackgateway.backend.slack.config.SlackSettings
import io.gatehill.slackgateway.backend.slack.exception.SlackErrorResponseException
import io.gatehill.slackgateway.backend.slack.model.ChannelsCreateResponse
import io.gatehill.slackgateway.backend.slack.model.ChannelsListResponse
import io.gatehill.slackgateway.backend.slack.model.GroupsCreateResponse
import io.gatehill.slackgateway.backend.slack.model.GroupsListResponse
import io.gatehill.slackgateway.backend.slack.model.SlackChannel
import io.gatehill.slackgateway.backend.slack.model.SlackChannelsCreateResponse
import io.gatehill.slackgateway.backend.slack.model.SlackGroup
import io.gatehill.slackgateway.backend.slack.model.SlackPublicChannel
import io.gatehill.slackgateway.backend.slack.model.SlackUser
import io.gatehill.slackgateway.backend.slack.model.SlackUserGroup
import io.gatehill.slackgateway.backend.slack.model.UserGroupsListResponse
import io.gatehill.slackgateway.backend.slack.model.UserGroupsUsersListResponse
import io.gatehill.slackgateway.backend.slack.model.UsersListResponse
import io.gatehill.slackgateway.model.ChannelType
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Common Slack operations.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SlackOperationsService @Inject constructor(
    private val slackApiService: SlackApiService
) {
    private val logger: Logger =
        LogManager.getLogger(SlackOperationsService::class.java)

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

        val reply =
            slackApiService.invokePaginatedSlackCommand<SlackUser, UsersListResponse>(
                commandName = "users.list"
            )
        return reply.members
    }

    private fun fetchUserGroups(): List<SlackUserGroup> {
        logger.debug("Fetching all user groups")
        val reply =
            slackApiService.invokeSlackCommand<UserGroupsListResponse>(
                commandName = "usergroups.list",
                method = SlackApiService.HttpMethod.GET
            )
        return reply.usergroups
    }

    internal fun listChannels(channelType: ChannelType): List<SlackChannel> =
        when (channelType) {
            ChannelType.PRIVATE -> listPrivateChannels()
            ChannelType.PUBLIC -> listPublicChannels()
        }

    private fun listPrivateChannels(): List<SlackGroup> {
        logger.debug("Listing private channels")
        val reply =
            slackApiService.invokePaginatedSlackCommand<SlackGroup, GroupsListResponse>(
                commandName = "conversations.list"
            )
        return reply.groups ?: emptyList()
    }

    private fun listPublicChannels(): List<SlackPublicChannel> {
        logger.debug("Listing public channels")
        val reply =
            slackApiService.invokePaginatedSlackCommand<SlackPublicChannel, ChannelsListResponse>(
                commandName = "conversations.list"
            )
        return reply.channels ?: emptyList()
    }

    internal fun createChannel(
        channelName: String,
        channelType: ChannelType
    ): SlackChannel {
        logger.debug("Creating $channelType channel: $channelName")

        val reply: SlackChannelsCreateResponse = try {
            when (channelType) {
                ChannelType.PRIVATE -> slackApiService.invokeSlackCommand<GroupsCreateResponse>(
                    commandName = "conversations.create",
                    params = mapOf(
                        "name" to channelName,
                        "validate" to "true"
                    )
                )
                ChannelType.PUBLIC -> slackApiService.invokeSlackCommand<ChannelsCreateResponse>(
                    commandName = "conversations.create",
                    params = mapOf(
                        "name" to channelName,
                        "validate" to "true"
                    )
                )
            }

        } catch (e: SlackErrorResponseException) {
            // specifically handle case where group exists but bot cannot see it
            if (e.errorResponse?.error == "name_taken") {
                throw IllegalStateException(
                    "Unable to create a $channelType channel named '$channelName' as one already exists. This is usually because the Slack token you are using does not have permission to access an existing channel. Add permission to access the '$channelName' channel to this token and try again."
                )
            } else {
                throw e
            }
        }

        logger.debug("Create $channelType channel response: $reply")
        logger.info("Channel $channelName ($channelType) created")
        return reply.channel
    }

    internal fun inviteToChannel(
        channel: SlackChannel,
        channelType: ChannelType,
        memberId: String
    ) {
        slackApiService.invokeSlackCommand<Map<String, Any>>(
            commandName = when (channelType) {
                ChannelType.PRIVATE -> "conversations.invite"
                ChannelType.PUBLIC -> "conversations.invite"
            },
            params = mapOf(
                "channel" to channel.id,
                "user" to memberId
            )
        )
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

        val reply =
            slackApiService.invokeSlackCommand<UserGroupsUsersListResponse>(
                commandName = "usergroups.users.list",
                method = SlackApiService.HttpMethod.GET,
                params = mapOf(
                    "usergroup" to userGroup.id
                )
            )

        return reply.users
    }

    internal fun sendMessage(params: Map<String, Any?>) {
        logger.info("Forwarding message to channel '${params["channel"]}': $params")

        slackApiService.invokeSlackCommand<Map<String, Any>>(
            commandName = "chat.postMessage",
            params = params,
            bodyMode = SlackApiService.BodyMode.JSON
        )
    }
}
