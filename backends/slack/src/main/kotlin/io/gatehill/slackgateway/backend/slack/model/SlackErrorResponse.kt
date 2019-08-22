package io.gatehill.slackgateway.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Example:
 * ```
 * {"ok":false,"error":"name_taken","detail":"`name` is already taken.","warning":"superfluous_charset","response_metadata":{"warnings":["superfluous_charset"]}}"
 * ```
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackErrorResponse(
    override val ok: Boolean,

    val error: String?,
    val detail: String?
) : ResponseWithStatus
