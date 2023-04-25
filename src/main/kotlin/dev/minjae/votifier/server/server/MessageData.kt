package dev.minjae.votifier.server.server

data class MessageData(
    val op: Int,
    val payload: String,
) {
    enum class OpCode(val code: Int) {
        LOGIN_REQUEST(0),
        LOGIN_SUCCESS(1),
        LOGIN_FAILURE(2),
        MESSAGE(3),
        ;

        companion object {
            fun fromCode(code: Int): OpCode? = values().find { it.code == code }
        }
    }
}
