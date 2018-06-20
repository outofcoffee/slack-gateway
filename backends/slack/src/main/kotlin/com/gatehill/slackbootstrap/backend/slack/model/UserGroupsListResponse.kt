package com.gatehill.slackbootstrap.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserGroupsListResponse(
    val ok: Boolean,
    val usergroups: List<SlackUserGroup>
)
