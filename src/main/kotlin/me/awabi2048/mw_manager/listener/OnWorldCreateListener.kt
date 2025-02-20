package me.awabi2048.mw_manager.listener

import com.onarandombox.MultiverseCore.commands.WhoCommand
import me.awabi2048.mw_manager.Main.Companion.creationDataSet
import me.awabi2048.mw_manager.my_world.CreationLevel
import me.awabi2048.mw_manager.my_world.TemplateWorld
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerChatEvent

object OnWorldCreateListener : Listener {
    @EventHandler
    fun onChatInput(event: PlayerChatEvent) {
        val creationData = creationDataSet.find { it.player == event.player } ?: return

        // ワールド名を入力
        if (creationData.creationLevel == CreationLevel.WORLD_NAME) {
            val registerWorldName = event.message
            creationDataSet.find { it.player == event.player }!!.worldName = registerWorldName
            creationDataSet.find { it.player == event.player }!!.creationLevel = CreationLevel.CLONE_SOURCE
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val creationData = creationDataSet.find { it.player == event.whoClicked } ?: return
        val player = event.whoClicked as Player

        // ソース選択
        if (creationData.creationLevel == CreationLevel.CLONE_SOURCE) {
            val templateName = event.currentItem?.lore?.get(1)!!
            val templateWorld = TemplateWorld(templateName)

            // プレビュー
            if (event.isLeftClick && !event.isShiftClick) {
                //
                templateWorld.preview(player)
                player.sendMessage("§eプレビュー中")
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f)
            }

            // 選択
            if (event.isLeftClick && event.isShiftClick) {
                creationDataSet.find { it.player == player }!!.sourceWorldName = templateName
                creationDataSet.find { it.player == player }!!.creationLevel = CreationLevel.WAITING_CREATION
            }
        }
    }
}
