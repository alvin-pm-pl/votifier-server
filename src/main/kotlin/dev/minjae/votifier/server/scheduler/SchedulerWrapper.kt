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
