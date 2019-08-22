package io.gatehill.slackgateway.backend.slack.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue
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
        LogManager.getLogger(io.gatehill.slackgateway.backend.slack.service.SlackApiService::class.java)

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
        method: io.gatehill.slackgateway.backend.slack.service.SlackApiService.HttpMethod = io.gatehill.slackgateway.backend.slack.service.SlackApiService.HttpMethod.POST,
        bodyMode: io.gatehill.slackgateway.backend.slack.service.SlackApiService.BodyMode = io.gatehill.slackgateway.backend.slack.service.SlackApiService.BodyMode.FORM
    ) = invokeSlackCommand(commandName, params, R::class.java, method, bodyMode)

    fun <R> invokeSlackCommand(
        commandName: String,
        params: Map<String, *>,
        responseClass: Class<R>,
        method: io.gatehill.slackgateway.backend.slack.service.SlackApiService.HttpMethod,
        bodyMode: io.gatehill.slackgateway.backend.slack.service.SlackApiService.BodyMode
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
                    io.gatehill.slackgateway.backend.slack.service.SlackApiService.HttpMethod.GET -> {
                        uriBuilder.addParameters(buildNameValuePairs(params))
                        HttpGet(uriBuilder.build())
                    }
                    io.gatehill.slackgateway.backend.slack.service.SlackApiService.HttpMethod.POST -> {
                        HttpPost(uriBuilder.build()).apply {
                            when (bodyMode) {
                                io.gatehill.slackgateway.backend.slack.service.SlackApiService.BodyMode.FORM -> this.entity =
                                    UrlEncodedFormEntity(buildNameValuePairs(params), "UTF-8")

                                io.gatehill.slackgateway.backend.slack.service.SlackApiService.BodyMode.JSON -> {
                                    this.addHeader(
                                        "Authorization",
                                        "Bearer ${io.gatehill.slackgateway.backend.slack.config.SlackSettings.slackUserToken}"
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

    inline fun <reified T, reified R : io.gatehill.slackgateway.backend.slack.model.PaginatedResponse<T>> invokePaginatedSlackCommand(
        commandName: String,
        params: Map<String, *> = emptyMap<String, Any>(),
        method: io.gatehill.slackgateway.backend.slack.service.SlackApiService.HttpMethod = io.gatehill.slackgateway.backend.slack.service.SlackApiService.HttpMethod.POST,
        bodyMode: io.gatehill.slackgateway.backend.slack.service.SlackApiService.BodyMode = io.gatehill.slackgateway.backend.slack.service.SlackApiService.BodyMode.FORM
    ) = invokePaginatedSlackCommand(commandName, params, R::class.java, method, bodyMode)

    fun <T, R : io.gatehill.slackgateway.backend.slack.model.PaginatedResponse<T>> invokePaginatedSlackCommand(
        commandName: String,
        params: Map<String, *>,
        responseClass: Class<R>,
        method: io.gatehill.slackgateway.backend.slack.service.SlackApiService.HttpMethod,
        bodyMode: io.gatehill.slackgateway.backend.slack.service.SlackApiService.BodyMode
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
                jsonMapper.readValue<io.gatehill.slackgateway.backend.slack.model.SlackErrorResponse>(jsonResponse)
            throw io.gatehill.slackgateway.backend.slack.exception.SlackErrorResponseException(
                errorResponse,
                jsonResponse,
                e
            )
        }

        when (parsedResponse) {
            is io.gatehill.slackgateway.backend.slack.model.ResponseWithStatus -> if (!parsedResponse.ok) throw io.gatehill.slackgateway.backend.slack.exception.SlackErrorResponseException(
                jsonResponse
            )
            is Map<*, *> -> if (true != parsedResponse["ok"]) throw io.gatehill.slackgateway.backend.slack.exception.SlackErrorResponseException(
                jsonResponse
            )
            else -> logger.warn("Unable to check response for type: $responseClass - payload: $jsonResponse")
        }
        return parsedResponse
    }

    private fun buildNameValuePairs(params: Map<String, Any?>): List<NameValuePair> {
        val payload = mutableListOf<NameValuePair>()
        for ((key, value) in params) {
            if (key != "token") {
                payload.add(BasicNameValuePair(key, value?.toString()))
            }
        }
        payload += BasicNameValuePair(
            "token",
            io.gatehill.slackgateway.backend.slack.config.SlackSettings.slackUserToken
        )
        return payload
    }

    private fun generateJsonBody(params: Map<String, *>) = jsonMapper.writeValueAsString(params)
}
