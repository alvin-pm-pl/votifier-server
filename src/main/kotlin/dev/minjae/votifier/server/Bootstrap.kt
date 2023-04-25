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
