package me.awabi2048.mw_manager.ui

import me.awabi2048.mw_manager.my_world.MyWorld
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class MemberUI(val player: Player, val world: MyWorld): AbstractInteractiveUI(player) {
    override fun update() {
        val ui = MemberUI(player, world)
        ui.open(false)
    }

    override fun onClick(event: InventoryClickEvent) {
        TODO("Not yet implemented")
    }

    override fun preOpenProcess(firstOpen: Boolean) {
        TODO("Not yet implemented")
    }

    override fun construct(): Inventory {
        TODO("Not yet implemented")
    }

    override fun onClose(reason: InventoryCloseEvent.Reason) {
        TODO("Not yet implemented")
    }

}
