package io.gatehill.slackgateway.service

import io.gatehill.slackgateway.backend.slack.service.SlackApiService
import io.gatehill.slackgateway.backend.slack.service.SlackOperationsService
import io.gatehill.slackgateway.model.ChannelType
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be empty`
import org.amshove.kluent.`should not be null`
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

/**
 * Specification for `SlackOperationsService`.
 */
object SlackOperationsServiceSpec : Spek({
    describe("a service") {
        val api = SlackApiService()
        val service = SlackOperationsService(api)

        describe("listing users") {
            val users = service.users

            it("returns users") {
                users.`should not be empty`()
            }
        }

        describe("fetching a user group") {
            val userGroup = service.fetchUserGroup("botusers")

            it("returns a user group") {
                userGroup.`should not be null`()
                userGroup.handle `should be equal to` "botusers"
            }
        }

        describe("listing user group users") {
            val userGroup = service.fetchUserGroup("botusers")
            val userIds = service.listUserGroupUserIds(userGroup!!)

            it("returns user IDs") {
                userIds.`should not be empty`()
            }
        }

        describe("listing private channels") {
            val channels = service.listChannels(ChannelType.PRIVATE)

            it("returns channels") {
                channels.`should not be empty`()
            }
        }
    }
})
