package com.gatehill.slackbootstrap.http.chat

import com.gatehill.slackbootstrap.http.config.ChatSettings
import com.gatehill.slackbootstrap.service.InboundMessageService
import com.gatehill.slackbootstrap.service.OutboundMessageService
import io.vertx.core.*
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
open class HttpInboundMessageServiceImpl @Inject constructor(private val outboundMessageService: OutboundMessageService) : InboundMessageService {
    private val logger: Logger = LogManager.getLogger(HttpInboundMessageServiceImpl::class.java)
    private var server: HttpServer? = null

    override fun listenForEvents() {
        Vertx.vertx().apply {
            deployVerticle(object : AbstractVerticle() {
                override fun start(startFuture: Future<Void>) {
                    try {
                        val router = Router.router(vertx).apply { configureRoutes(vertx, this) }

                        logger.debug("Listening on ${ChatSettings.hostname}:${ChatSettings.port}")
                        server = vertx.createHttpServer(HttpServerOptions().setPort(ChatSettings.port).setHost(ChatSettings.hostname))
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
            routingContext.response().end("""
                <html>
                    <h3>Slack Bootstrap</h3>
                    <ul>
                        <li><a href="/post">/post</a></li>
                    </ul>
                </htm>
            """.trimIndent())
        }

        router.post("/post").handler { routingContext ->
            vertx.executeBlocking(
                    Handler<Future<String>> {
                        try {
                            it.complete(handle(routingContext))
                        } catch (e: Exception) {
                            it.fail(e)
                        }
                    },
                    Handler<AsyncResult<String?>> {
                        if (it.succeeded()) {
                            routingContext.response().end(it.result())
                        } else {
                            routingContext.fail(it.cause())
                        }
                    }
            )
        }
    }

    private fun handle(routingContext: RoutingContext): String {
        val channelName = routingContext.request().getParam("channel")
        outboundMessageService.forward(channelName, routingContext.bodyAsString)
        return "OK"
    }

    override fun stopListening() {
        server?.close()
        server = null
    }
}
