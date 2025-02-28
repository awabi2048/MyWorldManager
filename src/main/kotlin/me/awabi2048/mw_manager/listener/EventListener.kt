package me.awabi2048.mw_manager.listener

import me.awabi2048.mw_manager.data_file.Config
import me.awabi2048.mw_manager.my_world.MyWorldManager
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.server.ServerLoadEvent
import org.bukkit.event.world.WorldLoadEvent

object EventListener : Listener {
    // MyWorldははじめロードしないようにする
    @EventHandler
    fun onServerLoad(event: WorldLoadEvent) {
        MyWorldManager.unloadMyWorlds()
    }

    private fun updateTask(world: World) {
        // データファイルのアップデート
        MyWorldManager.updateData()

        // 無人ワールドのアンロード


        // ワールドの情報とファイル上のデータの同期
        if (world.name.startsWith("my_world.")) {
            val myWorld = MyWorldManager.registeredMyWorld.find {it.vanillaWorld == world}?: return
            myWorld.sync()
        }
    }

    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        updateTask(event.player.world)
    }

    @EventHandler
    fun onPlayerChangeWorld(event: PlayerChangedWorldEvent) {
        updateTask(event.player.world)
    }
}
