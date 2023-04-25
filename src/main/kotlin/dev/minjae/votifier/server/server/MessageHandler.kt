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
