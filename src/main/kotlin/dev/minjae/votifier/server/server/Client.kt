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
