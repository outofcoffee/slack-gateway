package com.gatehill.slackgateway.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GroupsListResponse(
    override val ok: Boolean,
    val groups: List<SlackGroup>?
) : ResponseWithStatus
