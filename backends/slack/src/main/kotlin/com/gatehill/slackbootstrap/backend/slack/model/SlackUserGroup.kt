package com.gatehill.slackbootstrap.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * This is a list of users.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackUserGroup(val id: String,
                          val handle: String,
                          val name: String)
