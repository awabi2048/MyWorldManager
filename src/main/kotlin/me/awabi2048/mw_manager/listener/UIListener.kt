package me.awabi2048.mw_manager.listener

import me.awabi2048.mw_manager.Main.Companion.playerUIState
import me.awabi2048.mw_manager.ui.AbstractInteractiveUI
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

object UIListener : Listener {
    @EventHandler
    fun onUIClick(event: InventoryClickEvent) {
        if (playerUIState.keys.contains(event.whoClicked)) event.isCancelled = true

        if (playerUIState[event.whoClicked] is AbstractInteractiveUI) {
            (playerUIState[event.whoClicked]!! as AbstractInteractiveUI).onClick(event)
        }
    }

    @EventHandler
    fun onUIClose(event: InventoryCloseEvent) {
        if (event.reason !in listOf(
                InventoryCloseEvent.Reason.PLUGIN,
                InventoryCloseEvent.Reason.OPEN_NEW
            ) && playerUIState.keys.contains(event.player)
        ) {
            playerUIState[event.player]!!.onClose(event.reason)
            playerUIState.remove(event.player)
        }
    }
}
