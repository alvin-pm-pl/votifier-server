package dev.minjae.votifier.server.server

import com.vexsoftware.votifier.model.Vote

data class VoteData(
    val serviceName: String,
    val username: String,
    val address: String,
    val timestamp: String,
) {

    companion object {
        @JvmStatic
        fun fromVote(vote: Vote): VoteData =
            VoteData(vote.serviceName, vote.username, vote.address, vote.timeStamp)
    }
}
