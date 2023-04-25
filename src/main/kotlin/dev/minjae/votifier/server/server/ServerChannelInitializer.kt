package dev.minjae.votifier.server.server

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel

class ServerChannelInitializer(private val udpServer: TCPServer) : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
        println(ch.remoteAddress())
        if (ch.remoteAddress() != null) {
            ch.pipeline().addLast(AuthHandler.NAME, AuthHandler(udpServer, ch))
        }
    }
}
