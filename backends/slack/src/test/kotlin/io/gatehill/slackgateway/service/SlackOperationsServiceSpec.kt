package io.gatehill.slackgateway.service

import io.gatehill.slackgateway.backend.slack.service.SlackApiService
import io.gatehill.slackgateway.backend.slack.service.SlackOperationsService
import io.gatehill.slackgateway.model.ChannelType
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be empty`
import org.amshove.kluent.`should not be null`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 * Specification for `SlackOperationsService`.
 */
object SlackOperationsServiceSpec : Spek({
    given("a service") {
        val api = SlackApiService()
        val service = SlackOperationsService(api)

        on("listing users") {
            val users = service.users

            it("returns users") {
                users.`should not be empty`()
            }
        }

        on("fetching a user group") {
            val userGroup = service.fetchUserGroup("botusers")

            it("returns a user group") {
                userGroup.`should not be null`()
                userGroup.handle `should be equal to` "botusers"
            }
        }

        on("listing user group users") {
            val userGroup = service.fetchUserGroup("botusers")
            val userIds = service.listUserGroupUserIds(userGroup!!)

            it("returns user IDs") {
                userIds.`should not be empty`()
            }
        }

        on("listing private channels") {
            val channels = service.listChannels(ChannelType.PRIVATE)

            it("returns channels") {
                channels.`should not be empty`()
            }
        }
    }
})
