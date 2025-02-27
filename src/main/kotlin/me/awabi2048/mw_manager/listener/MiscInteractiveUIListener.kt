package me.awabi2048.mw_manager.listener

import me.awabi2048.mw_manager.Main
import me.awabi2048.mw_manager.Main.Companion.confirmationTracker
import me.awabi2048.mw_manager.ui.ConfirmationTracker
import me.awabi2048.mw_manager.ui.ConfirmationUI
import me.awabi2048.mw_manager.ui.WarpShortcutUI
import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

object MiscInteractiveUIListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.view.title == "§8§lワープショートカット") {
            val ui = WarpShortcutUI(event.whoClicked as Player)
            ui.onClick(event)
        }
    }

    @EventHandler
    fun onConfirmationUIClick(event: InventoryClickEvent) {
        if (event.view.title != "§8§l確認") return
        val tracker = confirmationTracker.find {it.player == event.whoClicked}?: return
        ConfirmationUI(tracker.player, tracker.uiData).onClick(event)
    }
}
