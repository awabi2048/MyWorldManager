package me.awabi2048.mw_manager.listener

import me.awabi2048.mw_manager.Main
import me.awabi2048.mw_manager.Main.Companion.PREFIX
import me.awabi2048.mw_manager.Main.Companion.creationDataSet
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.my_world.world_property.WorldActivityState
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerLoginEvent
import java.time.LocalDate

object MiscListener : Listener {
    private fun updateTask(world: World) {
        // データファイルのアップデート
        MyWorldManager.updateData()
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

            val archivedWorlds = MyWorldManager.registeredMyWorlds
                .filter { player in it.members!! }
                .filter { it.activityState == WorldActivityState.ARCHIVED }

            archivedWorlds.forEach { world ->
                world.activityState = WorldActivityState.ACTIVE
            }
        }

        // ワールド作成中ならデータ消去
        if (creationDataSet.any {it.player == event.player}) {
            creationDataSet.removeIf {it.player == event.player}
        }
    }

    @EventHandler
    fun onPlayerChangeWorld(event: PlayerChangedWorldEvent) {
        // 各種ワールドのアップデート処理
        updateTask(event.player.world)

        // 移動先が MyWorld なら日時を更新する
        if (MyWorldManager.registeredMyWorlds.filter {it.activityState == WorldActivityState.ACTIVE}.any {it.vanillaWorld == event.player.world}) {
            val myWorld = MyWorldManager.registeredMyWorlds.find {it.vanillaWorld == event.player.world}?: return
            myWorld.update()
        }
    }
}
