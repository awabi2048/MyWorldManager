package me.awabi2048.mw_manager.listener

import me.awabi2048.mw_manager.my_world.MyWorldManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent

object EventListener : Listener {
    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        if (MyWorldManager.registeredMyWorld.any { it.owner == event.player }) {
            MyWorldManager.registeredMyWorld.filter { it.owner == event.player }.forEach { it.update() }
        }
    }
}
