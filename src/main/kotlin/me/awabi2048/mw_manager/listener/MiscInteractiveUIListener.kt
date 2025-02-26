package me.awabi2048.mw_manager.listener

import me.awabi2048.mw_manager.ui.WarpShortcutUI
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

object MiscInteractiveUIListener: Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.view.title == "§8§lWarp Shortcut") {
            val ui = WarpShortcutUI(event.whoClicked as Player)
            ui.onClick(event)
        }
    }
}
