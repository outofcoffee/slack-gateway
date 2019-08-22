package io.gatehill.slackgateway.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserGroupsListResponse(
    override val ok: Boolean,
    val usergroups: List<io.gatehill.slackgateway.backend.slack.model.SlackUserGroup>
) : io.gatehill.slackgateway.backend.slack.model.ResponseWithStatus
