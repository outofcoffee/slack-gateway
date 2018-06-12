package com.gatehill.slackbootstrap.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GroupsListResponse(val ok: Boolean,
                              val groups: List<SlackGroup>)
