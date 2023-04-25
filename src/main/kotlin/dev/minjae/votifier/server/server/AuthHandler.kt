package dev.minjae.votifier.server.server

import com.fasterxml.jackson.module.kotlin.readValue
import dev.minjae.votifier.server.jsonMapper
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.socket.SocketChannel
import io.netty.util.CharsetUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AuthHandler(val server: TCPServer, val channel: SocketChannel) : ChannelInboundHandlerAdapter() {

    private val logger: Logger = LoggerFactory.getLogger("AuthenticatedMessageHandler")

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, buf: Any) {
        if (buf is ByteBuf) {
            val data: MessageData = jsonMapper.readValue(buf.toString(CharsetUtil.UTF_8))
            val client =
                server.clients[ctx.channel().remoteAddress()] ?: throw IllegalStateException("Client not found")
            if (data.payload != server.password) {
                logger.info("Authentication failed from ${ctx.channel().remoteAddress()}")
                client.sendPacket(MessageData(MessageData.OpCode.LOGIN_FAILURE.code, "Authentication failed"))
                ctx.close()
                return
            }
            client.setAuthenticated()
            channel.writeAndFlush("AABBCCDDEE")
            client.sendPacket(MessageData(MessageData.OpCode.LOGIN_SUCCESS.code, "Authentication successful"))
            ctx.pipeline().remove(NAME)
            ctx.pipeline().addLast(MessageHandler.NAME, MessageHandler(server))
        }
    }

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        logger.info("Client attempted to connected: " + ctx.channel().remoteAddress())
        server.clients[ctx.channel().remoteAddress()] = Client(ctx, channel)
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        logger.info("Client disconnected: " + ctx.channel().remoteAddress())
        server.clients.remove(ctx.channel().remoteAddress())
    }

    @Deprecated("Deprecated in Java", ReplaceWith("ctx.close()"))
    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        ctx.close()
    }

    companion object {
        const val NAME = "AuthHandler"
    }
}
