package me.awabi2048.mw_manager.listener

import me.awabi2048.mw_manager.Main.Companion.PREFIX
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.my_world.world_property.WorldActivityState
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerLoginEvent
import java.time.LocalDate

object MiscListener : Listener {
    // MyWorldははじめロードしないように
    private fun updateTask(world: World) {
        // データファイルのアップデート
        MyWorldManager.updateData()

        // 無人ワールドのアンロード


        // ワールドの情報とファイル上のデータの同期
        if (world.name.startsWith("my_world.")) {
            val myWorld = MyWorldManager.registeredMyWorlds.find { it.vanillaWorld == world } ?: return
            myWorld.sync()
        }
    }

    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        // ワールドのアップデート処理
        updateTask(event.player.world)

        val player = event.player

        // Admin → 期限切れのワールドがあれば通知
        if (player.hasPermission("mw_manager.admin")) {
            val expiredWorldCount = MyWorldManager.registeredMyWorlds.count { it.isOutDated == true }
            if (expiredWorldCount != 0) player.sendMessage("$PREFIX §c§n${LocalDate.now()}時点で、${expiredWorldCount}個のワールドが期限切れ状態です。")
        }

        // アーカイブされたものがあれば、復元を試みる
        if (MyWorldManager.registeredMyWorlds.filter { player in it.members!! } .any { it.activityState == WorldActivityState.ARCHIVED }) {

            val archivedWorld = MyWorldManager.registeredMyWorlds.filter { player in it.members!! }
                .filter { it.activityState == WorldActivityState.ARCHIVED }

            archivedWorld.forEach { world ->
                world.activityState = WorldActivityState.ACTIVE
            }
        }

        //
    }

    @EventHandler
    fun onPlayerChangeWorld(event: PlayerChangedWorldEvent) {
        // 各種ワールドのアップデート処理
        updateTask(event.player.world)

        // 移動先が MyWorld なら日時を更新する
        if ((event.player.world) in MyWorldManager.registeredMyWorlds.filter {it.activityState == WorldActivityState.ARCHIVED} .map {it.vanillaWorld}) {
            val myWorld = MyWorldManager.registeredMyWorlds.find {it.vanillaWorld == event.player.world}?: return
            myWorld.update()
        }
    }
}
