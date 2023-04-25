package dev.minjae.votifier.server.logging

import com.vexsoftware.votifier.platform.LoggingAdapter
import org.slf4j.Logger

class LoggingWrapper(val logger: Logger) : LoggingAdapter {
    override fun error(s: String) {
        logger.error(s)
    }

    override fun error(s: String, vararg o: Any) {
        logger.error(s, o)
    }

    override fun error(s: String, e: Throwable, vararg o: Any) {
        logger.error(s, e, o)
    }

    override fun warn(s: String) {
        logger.warn(s)
    }

    override fun warn(s: String, vararg o: Any) {
        logger.warn(s, o)
    }

    override fun info(s: String) {
        logger.info(s)
    }

    override fun info(s: String, vararg o: Any) {
        logger.info(s, o)
    }
}
