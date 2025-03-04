package me.awabi2048.mw_manager.ui.abstract

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

abstract class AbstractInteractiveUI(player: Player): AbstractUI(player) {
    abstract fun onClick(event: InventoryClickEvent)
}
