package com.gatehill.slackgateway.http.chat

import com.fasterxml.jackson.module.kotlin.readValue
import com.gatehill.slackgateway.exception.HttpCodeException
import com.gatehill.slackgateway.http.config.ChatSettings
import com.gatehill.slackgateway.service.InboundMessageService
import com.gatehill.slackgateway.service.OutboundMessageService
import com.gatehill.slackgateway.util.jsonMapper
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.inject.Inject

/**
 * Handles HTTP conversations.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
open class HttpInboundMessageServiceImpl @Inject constructor(private val outboundMessageService: OutboundMessageService) :
    InboundMessageService {

    private val logger: Logger = LogManager.getLogger(HttpInboundMessageServiceImpl::class.java)
    private var server: HttpServer? = null

    override fun listenForEvents() {
        Vertx.vertx().apply {
            deployVerticle(object : AbstractVerticle() {
                override fun start(startFuture: Future<Void>) {
                    try {
                        val router = Router.router(vertx).apply { configureRoutes(vertx, this) }

                        logger.debug("Listening on ${ChatSettings.hostname}:${ChatSettings.port}")
                        server = vertx.createHttpServer(
                            HttpServerOptions().setPort(ChatSettings.port).setHost(ChatSettings.hostname)
                        )
                            .requestHandler(router::accept)
                            .listen({ listenResult ->
                                if (listenResult.succeeded()) startFuture.complete() else startFuture.fail(listenResult.cause())
                            })
                    } catch (e: Exception) {
                        startFuture.fail(e)
                    }
                }
            })
        }
    }

    private fun configureRoutes(vertx: Vertx, router: Router) {
        router.route().handler(BodyHandler.create())
        router.get("/").handler { routingContext ->
            routingContext.response().end(
                """
                <html>
                    <h3>Slack Gateway</h3>
                    <ul>
                        <li>/messages/raw</li>
                        <li>/messages/text</li>
                    </ul>
                </htm>
            """.trimIndent()
            )
        }

        router.post("/messages/raw").handler { routingContext ->
            handle(vertx, routingContext, this::handleRaw)
        }
        router.post("/messages/text").handler { routingContext ->
            handle(vertx, routingContext, this::handlePlain)
        }
        router.post("/post").handler { routingContext ->
            handle(vertx, routingContext, this::handleLegacy)
        }
    }

    private fun handle(vertx: Vertx, routingContext: RoutingContext, processor: (RoutingContext) -> String) {
        vertx.executeBlocking(
            Handler<Future<String>> {
                try {
                    it.complete(processor(routingContext))
                } catch (e: Exception) {
                    it.fail(e)
                }
            },
            Handler<AsyncResult<String?>> {
                if (it.succeeded()) {
                    routingContext.response().end(it.result())
                } else {
                    it.cause().let { cause ->
                        logger.error(
                            "Error handling ${routingContext.request().method()} request to ${routingContext.request().uri()}",
                            cause
                        )
                        when (cause) {
                            is HttpCodeException -> routingContext.response().setStatusCode(cause.code).end(cause.message)
                            else -> routingContext.fail(cause)
                        }
                    }
                }
            }
        )
    }

    private fun handleRaw(routingContext: RoutingContext): String {
        outboundMessageService.forward(routingContext.bodyAsString)
        return "Posted raw message"
    }

    private fun handlePlain(routingContext: RoutingContext): String {
        val channelName = try {
            routingContext.request().getParam("channel")
        } catch (e: Exception) {
            throw HttpCodeException(400, "Unable to parse channel name")
        }

        val message = mutableMapOf<String, Any>(
            "channel" to channelName
        )

        val text = buildTextMessage(routingContext)
        val showAsAttachment = routingContext.request().getParam("attachment") == "true"

        if (showAsAttachment) {
            val attachment = mutableMapOf(
                "text" to text
            )

            routingContext.request().getParam("color")?.let { color ->
                attachment += "color" to color.let { if (it.startsWith("#")) it else "#$it" }
            }

            attachment += arrayOf(
                "author_name",
                "footer",
                "footer_icon",
                "title",
                "title_link"
            ).mapNotNull { key ->
                routingContext.request().getParam(key)?.let { value -> key to value }
            }

            message += "attachments" to listOf(attachment)

        } else {
            message += "text" to text
        }

        outboundMessageService.forward(message)
        return "Posted plain message"
    }

    private fun buildTextMessage(routingContext: RoutingContext) =
        (routingContext.request().getParam("text") ?: "") +
                routingContext.request().params()
                    .filterNot { excludedParams.contains(it.key) }
                    .joinToString(" | ", transform = this::transformEntry)

    /**
     * Legacy endpoint to support old clients.
     */
    private fun handleLegacy(routingContext: RoutingContext): String {
        val channelName = routingContext.request().getParam("channel")
                ?: throw HttpCodeException(400, "No channel in request")

        val message = jsonMapper.readValue<Map<String, *>>(routingContext.bodyAsString).toMutableMap()
        message += "channel" to channelName

        outboundMessageService.forward(message)
        return "Posted legacy message"
    }

    private fun transformEntry(entry: MutableMap.MutableEntry<String, String>) =
        "${entry.key.replace("_", " ")}: *${entry.value}*"

    override fun stopListening() {
        server?.close()
        server = null
    }

    companion object {
        private val excludedParams = arrayOf(
            "attachment",
            "author_name",
            "channel",
            "color",
            "text",
            "title",
            "title_link"
        )
    }
}
