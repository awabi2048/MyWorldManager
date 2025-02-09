package me.awabi2048.mw_manager

import me.awabi2048.mw_manager.Main.Companion.registeredWorldData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent

object EventListener: Listener {
    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        val uuid = event.player.uniqueId.toString()
        if (registeredWorldData.contains(uuid)) {
            // update
            val myWorld = MyWorld(registeredWorldData.getString("$uuid.world_name") ?: return)
            myWorld.update()
        }
    }
}
