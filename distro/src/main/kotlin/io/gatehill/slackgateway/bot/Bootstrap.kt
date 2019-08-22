package io.gatehill.slackgateway.bot

import com.google.inject.AbstractModule
import com.google.inject.Guice.createInjector
import com.google.inject.Module
import io.gatehill.slackgateway.service.InboundMessageService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.inject.Inject
import kotlin.system.exitProcess

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class Bootstrap @Inject constructor(private val inboundMessageService: InboundMessageService) {
    /**
     * The main entrypoint.
     */
    fun start() {
        inboundMessageService.listenForEvents()

        Runtime.getRuntime().addShutdownHook(Thread {
            stop()
        })
    }

    private fun stop() {
        println("Terminating...")
        inboundMessageService.stopListening()
        println("Bye!")
        exitProcess(0)
    }

    /**
     * Constructs `Bootstrap` instances.
     */
    companion object Builder {
        private val logger: Logger = LogManager.getLogger(Builder::class.java)

        /**
         * Construct a new `Bootstrap` and wire up its dependencies.
         */
        fun build(vararg extensionModules: Module): Bootstrap {
            val modules = mutableListOf<Module>().apply {
                add(object : AbstractModule() {
                    override fun configure() {
                        extensionModules.forEach {
                            logger.debug("Installing injection module: ${it.javaClass.canonicalName}")
                            install(it)
                        }
                    }
                })
            }

            return createInjector(modules).getInstance(Bootstrap::class.java)
        }
    }
}
