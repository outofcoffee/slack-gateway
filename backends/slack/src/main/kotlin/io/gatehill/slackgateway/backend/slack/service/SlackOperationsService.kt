package io.gatehill.slackgateway.backend.slack.service

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
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
    private val slackApiService: io.gatehill.slackgateway.backend.slack.service.SlackApiService
) {
    private val logger: Logger =
        LogManager.getLogger(io.gatehill.slackgateway.backend.slack.service.SlackOperationsService::class.java)

    private val cache = CacheBuilder.newBuilder()
        .expireAfterWrite(io.gatehill.slackgateway.backend.slack.config.SlackSettings.cacheSeconds, TimeUnit.SECONDS)
        .build(object : CacheLoader<String, List<*>>() {
            override fun load(key: String) = when (key) {
                "users" -> fetchUsers()
                "userGroups" -> fetchUserGroups()
                else -> throw NotImplementedError()
            }
        })

    @Suppress("UNCHECKED_CAST")
    internal val users
        get() = cache["users"] as List<io.gatehill.slackgateway.backend.slack.model.SlackUser>

    @Suppress("UNCHECKED_CAST")
    private val userGroups
        get() = cache["userGroups"] as List<io.gatehill.slackgateway.backend.slack.model.SlackUserGroup>

    init {
        // force evaluation of token on startup
        io.gatehill.slackgateway.backend.slack.config.SlackSettings.slackUserToken
    }

    private fun fetchUsers(): List<io.gatehill.slackgateway.backend.slack.model.SlackUser> {
        logger.debug("Fetching all users")

        val reply =
            slackApiService.invokePaginatedSlackCommand<io.gatehill.slackgateway.backend.slack.model.SlackUser, io.gatehill.slackgateway.backend.slack.model.UsersListResponse>(
                commandName = "users.list"
            )
        return reply.members
    }

    private fun fetchUserGroups(): List<io.gatehill.slackgateway.backend.slack.model.SlackUserGroup> {
        logger.debug("Fetching all user groups")
        val reply =
            slackApiService.invokeSlackCommand<io.gatehill.slackgateway.backend.slack.model.UserGroupsListResponse>(
                commandName = "usergroups.list",
                method = io.gatehill.slackgateway.backend.slack.service.SlackApiService.HttpMethod.GET
            )
        return reply.usergroups
    }

    internal fun listChannels(channelType: ChannelType): List<io.gatehill.slackgateway.backend.slack.model.SlackChannel> =
        when (channelType) {
            ChannelType.PRIVATE -> listPrivateChannels()
            ChannelType.PUBLIC -> listPublicChannels()
        }

    private fun listPrivateChannels(): List<io.gatehill.slackgateway.backend.slack.model.SlackGroup> {
        logger.debug("Listing private channels")
        val reply =
            slackApiService.invokePaginatedSlackCommand<io.gatehill.slackgateway.backend.slack.model.SlackGroup, io.gatehill.slackgateway.backend.slack.model.GroupsListResponse>(
                commandName = "groups.list"
            )
        return reply.groups ?: emptyList()
    }

    private fun listPublicChannels(): List<io.gatehill.slackgateway.backend.slack.model.SlackPublicChannel> {
        logger.debug("Listing public channels")
        val reply =
            slackApiService.invokePaginatedSlackCommand<io.gatehill.slackgateway.backend.slack.model.SlackPublicChannel, io.gatehill.slackgateway.backend.slack.model.ChannelsListResponse>(
                commandName = "channels.list"
            )
        return reply.channels ?: emptyList()
    }

    internal fun createChannel(
        channelName: String,
        channelType: ChannelType
    ): io.gatehill.slackgateway.backend.slack.model.SlackChannel {
        logger.debug("Creating $channelType channel: $channelName")

        val reply: io.gatehill.slackgateway.backend.slack.model.SlackChannelsCreateResponse = try {
            when (channelType) {
                ChannelType.PRIVATE -> slackApiService.invokeSlackCommand<io.gatehill.slackgateway.backend.slack.model.GroupsCreateResponse>(
                    commandName = "groups.create",
                    params = mapOf(
                        "name" to channelName,
                        "validate" to "true"
                    )
                )
                ChannelType.PUBLIC -> slackApiService.invokeSlackCommand<io.gatehill.slackgateway.backend.slack.model.ChannelsCreateResponse>(
                    commandName = "channels.create",
                    params = mapOf(
                        "name" to channelName,
                        "validate" to "true"
                    )
                )
            }

        } catch (e: io.gatehill.slackgateway.backend.slack.exception.SlackErrorResponseException) {
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
        channel: io.gatehill.slackgateway.backend.slack.model.SlackChannel,
        channelType: ChannelType,
        memberId: String
    ) {
        slackApiService.invokeSlackCommand<Map<String, Any>>(
            commandName = when (channelType) {
                ChannelType.PRIVATE -> "groups.invite"
                ChannelType.PUBLIC -> "channels.invite"
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
    internal fun fetchUserGroup(userGroupHandle: String): io.gatehill.slackgateway.backend.slack.model.SlackUserGroup? {
        logger.debug("Fetching user group: $userGroupHandle")
        return userGroups.firstOrNull { it.handle.equals(userGroupHandle, ignoreCase = true) }
    }

    /**
     * @return the list of user IDs within the user group
     */
    internal fun listUserGroupUserIds(userGroup: io.gatehill.slackgateway.backend.slack.model.SlackUserGroup): List<String> {
        logger.debug("Listing user IDs in user group: ${userGroup.name}")

        val reply =
            slackApiService.invokeSlackCommand<io.gatehill.slackgateway.backend.slack.model.UserGroupsUsersListResponse>(
                commandName = "usergroups.users.list",
                method = io.gatehill.slackgateway.backend.slack.service.SlackApiService.HttpMethod.GET,
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
            bodyMode = io.gatehill.slackgateway.backend.slack.service.SlackApiService.BodyMode.JSON
        )
    }
}
