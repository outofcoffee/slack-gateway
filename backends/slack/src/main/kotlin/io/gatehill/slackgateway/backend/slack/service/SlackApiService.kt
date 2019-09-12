package io.gatehill.slackgateway.backend.slack.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue
import io.gatehill.slackgateway.backend.slack.config.SlackSettings
import io.gatehill.slackgateway.backend.slack.exception.SlackErrorResponseException
import io.gatehill.slackgateway.backend.slack.model.PaginatedResponse
import io.gatehill.slackgateway.backend.slack.model.ResponseWithStatus
import io.gatehill.slackgateway.backend.slack.model.SlackErrorResponse
import io.gatehill.slackgateway.util.jsonMapper
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.charset.Charset

/**
 * Interacts with the Slack API.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SlackApiService {
    private val logger: Logger =
        LogManager.getLogger(SlackApiService::class.java)

    enum class HttpMethod {
        GET,
        POST
    }

    enum class BodyMode {
        FORM,
        JSON
    }

    inline fun <reified R> invokeSlackCommand(
        commandName: String,
        params: Map<String, *> = emptyMap<String, Any>(),
        method: HttpMethod = HttpMethod.POST,
        bodyMode: BodyMode = BodyMode.FORM
    ) = invokeSlackCommand(commandName, params, R::class.java, method, bodyMode)

    fun <R> invokeSlackCommand(
        commandName: String,
        params: Map<String, *>,
        responseClass: Class<R>,
        method: HttpMethod,
        bodyMode: BodyMode
    ): R {

        HttpClientBuilder.create().build().use { httpClient ->
            // invoke command
            try {
                val uriBuilder = URIBuilder().apply {
                    scheme = "https"
                    host = "slack.com"
                    path = "/api/$commandName"
                }

                val request = when (method) {
                    HttpMethod.GET -> {
                        uriBuilder.addParameters(buildNameValuePairs(commandName, params))
                        HttpGet(uriBuilder.build())
                    }
                    HttpMethod.POST -> {
                        HttpPost(uriBuilder.build()).apply {
                            when (bodyMode) {
                                BodyMode.FORM -> this.entity =
                                    UrlEncodedFormEntity(buildNameValuePairs(commandName, params), "UTF-8")

                                BodyMode.JSON -> {
                                    this.addHeader(
                                        "Authorization",
                                        "Bearer ${determineToken(commandName)}"
                                    )
                                    this.entity = StringEntity(generateJsonBody(params), ContentType.APPLICATION_JSON)
                                }
                            }
                        }
                    }
                }

                httpClient.execute(request).let { response ->
                    response.entity.content.use {
                        val jsonResponse = String(it.readBytes(), Charset.forName("UTF-8"))
                        logger.debug("Slack API: $commandName returned HTTP status: ${response.statusLine.statusCode}")
                        logger.trace("Slack API: $commandName returned: $jsonResponse")

                        return parseResponse(jsonResponse, responseClass)
                    }
                }
            } catch (e: Exception) {
                throw RuntimeException("Error calling Slack API: $commandName", e)
            }
        }
    }

    inline fun <reified T, reified R : PaginatedResponse<T>> invokePaginatedSlackCommand(
        commandName: String,
        params: Map<String, *> = emptyMap<String, Any>(),
        method: HttpMethod = HttpMethod.POST,
        bodyMode: BodyMode = BodyMode.FORM
    ) = invokePaginatedSlackCommand(commandName, params, R::class.java, method, bodyMode)

    fun <T, R : PaginatedResponse<T>> invokePaginatedSlackCommand(
        commandName: String,
        params: Map<String, *>,
        responseClass: Class<R>,
        method: HttpMethod,
        bodyMode: BodyMode
    ): R {
        var fetchIteration = 0
        var firstResponse: R? = null
        var lastResponse: R? = null
        do {
            logger.debug("Invoking paged Slack API: $commandName [page ${++fetchIteration}]")

            val combinedParams = params.toMutableMap()
            lastResponse?.responseMetadata?.nextCursor?.let { combinedParams["cursor"] = it }

            lastResponse = invokeSlackCommand(
                commandName,
                combinedParams,
                responseClass,
                method,
                bodyMode
            )

            if (null == firstResponse) {
                firstResponse = lastResponse
            } else {
                firstResponse.innerResults += lastResponse.innerResults
            }
        } while (!lastResponse?.responseMetadata?.nextCursor.isNullOrEmpty())

        return firstResponse!!
    }

    private fun <R> parseResponse(
        jsonResponse: String,
        responseClass: Class<R>
    ): R {
        val parsedResponse = try {
            jsonMapper.readValue(jsonResponse, responseClass)
        } catch (e: JsonProcessingException) {
            // parsing may have failed if the response is an error response
            val errorResponse =
                jsonMapper.readValue<SlackErrorResponse>(jsonResponse)
            throw SlackErrorResponseException(
                errorResponse,
                jsonResponse,
                e
            )
        }

        when (parsedResponse) {
            is ResponseWithStatus -> if (!parsedResponse.ok) throw SlackErrorResponseException(
                jsonResponse
            )
            is Map<*, *> -> if (true != parsedResponse["ok"]) throw SlackErrorResponseException(
                jsonResponse
            )
            else -> logger.warn("Unable to check response for type: $responseClass - payload: $jsonResponse")
        }
        return parsedResponse
    }

    private fun buildNameValuePairs(commandName: String, params: Map<String, Any?>): List<NameValuePair> {
        val payload = mutableListOf<NameValuePair>()
        for ((key, value) in params) {
            if (key != "token") {
                payload.add(BasicNameValuePair(key, value?.toString()))
            }
        }
        payload += BasicNameValuePair(
            "token",
            determineToken(commandName)
        )
        return payload
    }

    /**
     * Determine the token to use, preferring the bot token, if set.
     */
    private fun determineToken(commandName: String) = when {
        USER_TOKEN_ONLY.contains(commandName) -> SlackSettings.slackUserToken
        else -> SlackSettings.slackBotToken ?: SlackSettings.slackUserToken
    }

    private fun generateJsonBody(params: Map<String, *>) = jsonMapper.writeValueAsString(params)

    private companion object {
        /**
         * API methods that require a user token with appropriate permissions,
         * i.e. a bot token will not suffice.
         */
        val USER_TOKEN_ONLY = arrayOf(
            "groups.create",
            "channels.create",
            "usergroups.list",
            "usergroups.users.list",
            "groups.invite",
            "channels.invite"
        )
    }
}
