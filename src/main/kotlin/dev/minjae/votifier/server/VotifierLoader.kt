package dev.minjae.votifier.server

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.vexsoftware.votifier.VoteHandler
import com.vexsoftware.votifier.model.Vote
import com.vexsoftware.votifier.net.VotifierServerBootstrap
import com.vexsoftware.votifier.net.VotifierSession
import com.vexsoftware.votifier.net.protocol.v1crypto.RSAIO
import com.vexsoftware.votifier.net.protocol.v1crypto.RSAKeygen
import com.vexsoftware.votifier.platform.LoggingAdapter
import com.vexsoftware.votifier.platform.VotifierPlugin
import com.vexsoftware.votifier.platform.scheduler.VotifierScheduler
import com.vexsoftware.votifier.support.forwarding.ForwardedVoteListener
import com.vexsoftware.votifier.support.forwarding.ForwardingVoteSink
import com.vexsoftware.votifier.util.IOUtil
import com.vexsoftware.votifier.util.KeyCreator
import com.vexsoftware.votifier.util.TokenUtil
import dev.minjae.votifier.server.logging.LoggingWrapper
import dev.minjae.votifier.server.scheduler.SchedulerWrapper
import dev.minjae.votifier.server.server.MessageData
import dev.minjae.votifier.server.server.TCPServer
import dev.minjae.votifier.server.server.VoteData
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.security.Key
import java.security.KeyPair
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.exitProcess

class VotifierLoader(private val udpServer: TCPServer) : VoteHandler, VotifierPlugin, ForwardedVoteListener {
    /**
     * The server bootstrap.
     */
    private lateinit var bootstrap: VotifierServerBootstrap

    private val logger by lazy { LoggerFactory.getLogger(VotifierLoader::class.java) }

    /**
     * The RSA key pair.
     */
    private lateinit var keyPair: KeyPair

    /**
     * Debug mode flag
     */
    private var debug = false

    /**
     * Keys used for websites.
     */
    private val tokens: HashMap<String, Key> = HashMap()

    private lateinit var forwardingMethod: ForwardingVoteSink
    private lateinit var scheduler: VotifierScheduler

    private lateinit var pluginLogger: LoggingAdapter

    fun onEnable() {
        scheduler = SchedulerWrapper()
        pluginLogger = LoggingWrapper(LoggerFactory.getLogger("Votifier"))
        val dataFolder = Paths.get(System.getProperty("user.dir")).toFile()
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdir()) {
                throw RuntimeException("Unable to create the plugin data folder $dataFolder")
            }
        }

        // Handle configuration.

        // Handle configuration.
        val config = File(dataFolder, "config.yml")

        /*
         * Use IP address from server.properties as a default for
         * configurations. Do not use InetAddress.getLocalHost() as it most
         * likely will return the main server address instead of the address
         * assigned to the server.
         */

        /*
         * Use IP address from server.properties as a default for
         * configurations. Do not use InetAddress.getLocalHost() as it most
         * likely will return the main server address instead of the address
         * assigned to the server.
         */
        var hostAddr: String = "0.0.0.0" // TODO: configurable
        if (hostAddr.isEmpty()) hostAddr = "0.0.0.0"

        /*
         * Create configuration file if it does not exists; otherwise, load it
         */

        /*
         * Create configuration file if it does not exists; otherwise, load it
         */
        if (!config.exists()) {
            try {
                this::class.java.getResourceAsStream("/config.yml")?.use {
                    config.apply {
                        if (!exists()) {
                            createNewFile()
                            appendBytes(it.readBytes())
                        }
                    }
                }
                // First time run - do some initialization.
                logger.info("Configuring Votifier for the first time...")

                // Initialize the configuration file.

                // Load and manually replace variables in the configuration.
                var cfgStr = String(IOUtil.readAllBytes(config.inputStream()), StandardCharsets.UTF_8)
                val token: String = TokenUtil.newToken()
                cfgStr = cfgStr.replace("%default_token%", token).replace("%ip%", hostAddr)
                config.writeText(cfgStr, StandardCharsets.UTF_8)

                /*
                 * Remind hosted server admins to be sure they have the right
                 * port number.
                 */
                logger.info("------------------------------------------------------------------------------")
                logger.info("Assigning NuVotifier to listen on port 8192. If you are hosting Craftbukkit on a")
                logger.info("shared server please check with your hosting provider to verify that this port")
                logger.info("is available for your use. Chances are that your hosting provider will assign")
                logger.info("a different port, which you need to specify in config.yml")
                logger.info("------------------------------------------------------------------------------")
                logger.info("Your default NuVotifier token is $token.")
                logger.info("You will need to provide this token when you submit your server to a voting")
                logger.info("list.")
                logger.info("------------------------------------------------------------------------------")
            } catch (ex: Exception) {
                logger.error("Error creating configuration file", ex)
                return
            }
        }

        val rsaDirectory = File(dataFolder, "rsa")

        // Load configuration.

        // Load configuration.
        val cfg = YAMLMapper().readTree(config.inputStream())

        /*
         * Create RSA directory and keys if it does not exist; otherwise, read
         * keys.
         */

        /*
         * Create RSA directory and keys if it does not exist; otherwise, read
         * keys.
         */
        try {
            if (!rsaDirectory.exists()) {
                if (!rsaDirectory.mkdir()) {
                    throw RuntimeException("Unable to create the RSA key folder $rsaDirectory")
                }
                keyPair = RSAKeygen.generate(2048)
                RSAIO.save(rsaDirectory, keyPair)
            } else {
                keyPair = RSAIO.load(rsaDirectory)
            }
        } catch (ex: Exception) {
            logger.error(
                "Error reading configuration file or RSA tokens",
                ex,
            )
            return
        }

        // the quiet flag always runs priority to the debug flag

        // the quiet flag always runs priority to the debug flag
        debug = cfg.get("debug")?.asBoolean() ?: false

        // Load Votifier tokens.
        // Load Votifier tokens.
        var tokenSection = cfg.get("tokens")

        if (tokenSection != null) {
//            val websites: Map<String, Any> = tokenSection
            for ((key, value) in tokenSection.fields()) {
                tokens[key] = KeyCreator.createKeyFrom(value.asText())
                logger.info("Loaded token for website: $key")
            }
        } else {
            val token = TokenUtil.newToken()
//            tokenSection = cfg["tokens", HashMap<String, String>()] as HashMap<String, String>
            tokenSection = cfg.get("tokens")
//            tokenSection["default"] = token
            (tokenSection as ObjectNode).put("default", token)
//            cfg.set("tokens", tokenSection)
            (cfg as ObjectNode).set<JsonNode>("tokens", tokenSection)
            tokens["default"] = KeyCreator.createKeyFrom(token)
            try {
                val mapper = YAMLMapper()
                mapper.writeValue(config, cfg)
            } catch (e: IOException) {
                logger.error(
                    "Error generating Votifier token",
                    e,
                )
                return
            }
            logger.info("------------------------------------------------------------------------------")
            logger.info("No tokens were found in your configuration, so we've generated one for you.")
            logger.info("Your default Votifier token is $token.")
            logger.info("You will need to provide this token when you submit your server to a voting")
            logger.info("list.")
            logger.info("------------------------------------------------------------------------------")
        }

        // Initialize the receiver.

        // Initialize the receiver.
        val host = cfg.get("host").asText()
        val port = cfg.get("port").asInt()
        if (!debug) logger.info("QUIET mode enabled!")

        if (port >= 0) {
            val disablev1 = cfg.get("disable-v1-protocol").asBoolean()
            if (disablev1) {
                logger.info("------------------------------------------------------------------------------")
                logger.info("Votifier protocol v1 parsing has been disabled. Most voting websites do not")
                logger.info("currently support the modern Votifier protocol in NuVotifier.")
                logger.info("------------------------------------------------------------------------------")
            }
            bootstrap = VotifierServerBootstrap(host, port, this, disablev1)
            bootstrap.start { error: Throwable? ->
                if (error != null) {
                    logger.error(
                        "An unknown error occured whilst starting Votifier server",
                        error,
                    )
                }
            }
        } else {
            logger.info("------------------------------------------------------------------------------")
            logger.info("Your Votifier port is less than 0, so we assume you do NOT want to start the")
            logger.info("votifier port server!")
            logger.info("------------------------------------------------------------------------------")
            exitProcess(1)
        }
    }

    fun onDisable() {
        bootstrap.shutdown()
    }

    override fun getScheduler(): VotifierScheduler {
        return scheduler
    }

    override fun onVoteReceived(
        vote: Vote,
        protocolVersion: VotifierSession.ProtocolVersion,
        remoteAddress: String,
    ) {
        if (debug) {
            logger.info("Got a " + protocolVersion.humanReadable + " vote record from " + remoteAddress + " -> " + vote)
        }
        fireVotifierEvent(vote)
    }

    override fun getTokens(): MutableMap<String, Key> {
        return tokens
    }

    override fun getProtocolV1Key(): KeyPair {
        return keyPair
    }

    override fun getPluginLogger(): LoggingAdapter {
        return pluginLogger
    }

    override fun onForward(v: Vote) {
    }

    private fun fireVotifierEvent(vote: Vote) {
        logger.info("Vote received from " + vote.username + " at " + vote.address + " for " + vote.serviceName)
        for ((_, client) in udpServer.clients) {
            if (client.isAuthenticated()) {
                client.sendPacket(
                    MessageData(
                        MessageData.OpCode.MESSAGE.code,
                        jsonMapper.writeValueAsString(
                            VoteData.fromVote(vote),
                        ),
                    ),
                )
            }
        }
    }

    companion object {
        val votedPlayers: MutableMap<String, Vote> = ConcurrentHashMap()
    }
}
