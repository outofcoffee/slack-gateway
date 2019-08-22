package io.gatehill.slackgateway.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserGroupsUsersListResponse(
    override val ok: Boolean,

    /**
     * User IDs only.
     */
    val users: List<String>
) : io.gatehill.slackgateway.backend.slack.model.ResponseWithStatus
