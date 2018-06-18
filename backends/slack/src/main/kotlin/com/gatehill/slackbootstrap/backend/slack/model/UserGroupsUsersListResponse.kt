package com.gatehill.slackbootstrap.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserGroupsUsersListResponse(val ok: Boolean,

                                       /**
                                        * User IDs only.
                                        */
                                       val users: List<String>
)
