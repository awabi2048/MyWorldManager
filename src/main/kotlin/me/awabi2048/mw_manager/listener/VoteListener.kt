package me.awabi2048.mw_manager.listener

import com.bencodez.votingplugin.events.PlayerVoteEvent
import me.awabi2048.mw_manager.player_data.PlayerData
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object VoteListener: Listener {
    @EventHandler
    fun onPlayerVote(event: PlayerVoteEvent) {
        val player = Bukkit.getOfflinePlayer(event.player).player?: return

        val playerData = PlayerData(player)
        playerData.worldPoint += 1

        println("voted!")
    }
}
