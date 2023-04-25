/**
 * The MIT License (MIT)
 *
 * Copyright 2023 alvin0319
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package dev.minjae.votifier.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.blackbird.BlackbirdModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dev.minjae.votifier.server.config.ServerConfig
import dev.minjae.votifier.server.server.TCPServer
import java.io.File

val jsonMapper: ObjectMapper = jacksonObjectMapper().registerKotlinModule()
    .registerModule(BlackbirdModule())

fun main() {
    val config: ServerConfig = File("config.json").apply {
        if (!exists()) {
            createNewFile()
            appendBytes({}.javaClass.getResourceAsStream("/config.json")!!.readBytes())
        }
    }.bufferedReader().use(jsonMapper::readValue)
    val server = TCPServer(config)

    val loader = VotifierLoader(server)
    loader.onEnable()

    Runtime.getRuntime().addShutdownHook(
        Thread {
            server.close()
            loader.onDisable()
        },
    )
}
