package dev.minjae.votifier.server.server

import com.fasterxml.jackson.module.kotlin.readValue
import dev.minjae.votifier.server.jsonMapper
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.socket.DatagramPacket
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MessageHandler(private val udpServer: TCPServer) : SimpleChannelInboundHandler<DatagramPacket>() {

    private val logger: Logger = LoggerFactory.getLogger("MessageHandler")

    override fun channelRead0(ctx: ChannelHandlerContext, msg: DatagramPacket) {
        try {
            val client =
                udpServer.clients[ctx.channel().remoteAddress()] ?: throw IllegalStateException("Client not found")
            val message = msg.content().toString(Charsets.UTF_8)
            val data: MessageData = jsonMapper.readValue(message)
            if (MessageData.OpCode.fromCode(data.op) != MessageData.OpCode.MESSAGE) {
                logger.info(
                    "Invalid opcode received from client $client, expected ${MessageData.OpCode.MESSAGE}, got ${
                        MessageData.OpCode.fromCode(
                            data.op,
                        )
                    }",
                )
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        logger.info("Client disconnected: " + ctx.channel().remoteAddress())
        udpServer.clients.remove(ctx.channel().remoteAddress())
    }

    @Deprecated("Deprecated in Java", ReplaceWith("ctx.close()"))
    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        ctx.close()
    }

    companion object {
        const val NAME = "MessageHandler"
    }
}
