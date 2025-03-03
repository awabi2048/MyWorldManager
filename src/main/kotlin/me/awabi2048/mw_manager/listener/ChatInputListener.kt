package me.awabi2048.mw_manager.listener

import me.awabi2048.mw_manager.Main.Companion.playerUIState
import me.awabi2048.mw_manager.ui.ChatInputInterface
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChatEvent

object ChatInputListener: Listener {
    @EventHandler
    fun onChatInput(event: PlayerChatEvent) {
        val playerUI = playerUIState[event.player]
        if (playerUI is ChatInputInterface) {
            (playerUI as ChatInputInterface).onChatInput(event.message)
            event.isCancelled = true
        }
    }
}
