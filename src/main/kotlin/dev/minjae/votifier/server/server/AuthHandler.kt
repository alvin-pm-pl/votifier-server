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
