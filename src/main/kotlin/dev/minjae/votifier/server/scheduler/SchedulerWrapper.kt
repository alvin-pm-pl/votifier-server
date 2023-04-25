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
package dev.minjae.votifier.server.scheduler

import com.vexsoftware.votifier.platform.scheduler.ScheduledVotifierTask
import com.vexsoftware.votifier.platform.scheduler.VotifierScheduler
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class SchedulerWrapper : VotifierScheduler {

    val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    override fun sync(runnable: Runnable): ScheduledVotifierTask {
        val future = scheduler.schedule(runnable, 0, TimeUnit.MILLISECONDS)
        return ScheduledTask(future)
    }

    override fun onPool(runnable: Runnable): ScheduledVotifierTask {
        val future = scheduler.schedule(runnable, 0, TimeUnit.MILLISECONDS)
        return ScheduledTask(future)
    }

    override fun delayedSync(runnable: Runnable, delay: Int, unit: TimeUnit): ScheduledVotifierTask {
        val future = scheduler.schedule(runnable, delay.toLong(), unit)
        return ScheduledTask(future)
    }

    override fun delayedOnPool(runnable: Runnable, delay: Int, unit: TimeUnit): ScheduledVotifierTask {
        val future = scheduler.schedule(runnable, delay.toLong(), unit)
        return ScheduledTask(future)
    }

    override fun repeatOnPool(runnable: Runnable, delay: Int, repeat: Int, unit: TimeUnit): ScheduledVotifierTask {
        val future = scheduler.scheduleAtFixedRate(runnable, delay.toLong(), repeat.toLong(), unit)
        return ScheduledTask(future)
    }

    data class ScheduledTask(val future: ScheduledFuture<*>) : ScheduledVotifierTask {
        override fun cancel() {
            future.cancel(true)
        }
    }
}
