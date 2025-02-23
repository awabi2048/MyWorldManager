package me.awabi2048.mw_manager.listener

import me.awabi2048.mw_manager.Main.Companion.creationDataSet
import me.awabi2048.mw_manager.config.DataFiles
import me.awabi2048.mw_manager.my_world.CreationLevel
import me.awabi2048.mw_manager.my_world.TemplateWorld
import me.awabi2048.mw_manager.ui.TemplateSelectUI
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerChatEvent

object WorldCreationSessionListener : Listener {
    @EventHandler
    fun onChatInput(event: PlayerChatEvent) {
        val creationData = creationDataSet.find { it.player == event.player }!!

        // ワールド名を入力
        if (creationData.creationLevel == CreationLevel.WORLD_NAME) {
            val registerWorldName = event.message

            // ブラックリスト判定
            val worldNameBlacklist = DataFiles.config.getStringList("world_name_blacklist")
            if (worldNameBlacklist.any {registerWorldName.contains(it)}) {
                event.player.sendMessage("§c使用できない文字列が含まれています。再度入力してください。")
                return
        }

            creationDataSet.find { it.player == event.player }!!.worldName = registerWorldName
            creationDataSet.find { it.player == event.player }!!.creationLevel = CreationLevel.CLONE_SOURCE

            event.player.sendMessage("§aワールドの名前を${event.message}に設定しました！")
            event.isCancelled = true

            // ソース選択メニュー
            val templateSelectUI = TemplateSelectUI(event.player)
            templateSelectUI.open()
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val creationData = creationDataSet.find { it.player == event.whoClicked } ?: return
        val player = event.whoClicked as Player

        // ソース選択
        if (creationData.creationLevel == CreationLevel.CLONE_SOURCE) {
            event.isCancelled = true

            val templateName = (event.currentItem?.itemMeta?.lore?.get(1)?: return).drop(2)
            val templateWorld = TemplateWorld(templateName)

            // プレビュー
            if (event.isLeftClick && !event.isShiftClick) {
                //
                templateWorld.preview(player)
                player.sendMessage("§e${templateWorld.worldName}§7をプレビュー中...")
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f)
            }

            // 選択
            if (event.isLeftClick && event.isShiftClick) {
                // データを保存
                creationDataSet.find { it.player == player }!!.sourceWorldName = templateName
                creationDataSet.find { it.player == player }!!.creationLevel = CreationLevel.WAITING_CREATION

                // 確認画面に移行（する予定）
                val creationSession = creationDataSet.find { it.player == player }!!

                println("register world: PLAYER: ${creationSession.player}, WORLD NAME: ${creationSession.worldName}, SOURCE: ${creationSession.sourceWorldName}")

                creationSession.register()
            }
        }

        // 確認表示

    }
}
