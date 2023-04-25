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
