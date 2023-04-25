package dev.minjae.votifier.server.server

import dev.minjae.votifier.server.jsonMapper
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.SocketChannel
import org.slf4j.LoggerFactory
import java.net.SocketAddress
import java.util.concurrent.atomic.AtomicBoolean

class Client(private val ctx: ChannelHandlerContext, private val channel: SocketChannel) {
    val address: SocketAddress = ctx.channel().remoteAddress()

    private val logger = LoggerFactory.getLogger("Client#$address")

    private val authenticated: AtomicBoolean = AtomicBoolean(false)

    private fun sendMessage() {
    }

    fun isAuthenticated(): Boolean = authenticated.get()

    fun setAuthenticated() {
        authenticated.set(true)
        logger.info("Authenticate successful")
    }

    fun sendPacket(data: MessageData) {
        logger.info("Sending Packet")
        val message = jsonMapper.writeValueAsString(data)
        channel.writeAndFlush(Unpooled.copiedBuffer(message, Charsets.UTF_8))
    }

    override fun toString(): String {
        return "Client(address=$address)"
    }
}
