package io.gatehill.slackgateway.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

/**
 * Converts JSON to objects.
 */
val jsonMapper by lazy { ObjectMapper().enable(JsonParser.Feature.ALLOW_COMMENTS).registerKotlinModule() }
