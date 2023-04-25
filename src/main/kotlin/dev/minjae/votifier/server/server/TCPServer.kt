package dev.minjae.votifier.server.server

import dev.minjae.votifier.server.config.ServerConfig
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.SocketAddress
import java.util.concurrent.ConcurrentHashMap

class TCPServer(config: ServerConfig) {
    val password = config.password

    private val logger: Logger = LoggerFactory.getLogger("UDPServer")

    val bossGroup: EventLoopGroup = NioEventLoopGroup()
    val workerGroup: EventLoopGroup = NioEventLoopGroup()

    private val bootstrap: ServerBootstrap = ServerBootstrap()
        .group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel::class.java)
        .childHandler(ServerChannelInitializer(this))
        .option(ChannelOption.SO_BACKLOG, 1024)
        .childOption(ChannelOption.SO_KEEPALIVE, true)

    private val future: ChannelFuture = bootstrap.bind(config.host, config.port).awaitUninterruptibly()

    init {
        logger.info("Server is listening on ${future.channel().localAddress()}")
    }

    val clients: ConcurrentHashMap<SocketAddress, Client> = ConcurrentHashMap()

    fun close() {
        future.channel().close().awaitUninterruptibly()
        bossGroup.shutdownGracefully().awaitUninterruptibly()
        workerGroup.shutdownGracefully().awaitUninterruptibly()
    }
}
