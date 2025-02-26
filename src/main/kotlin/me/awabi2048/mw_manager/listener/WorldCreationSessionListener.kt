package me.awabi2048.mw_manager.listener

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.creationDataSet
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.data_file.DataFiles
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
        val creationData = creationDataSet.find { it.player == event.player } ?: return

        // ワールド名を入力
        if (creationData.creationLevel == CreationLevel.WORLD_NAME) {
            val registerWorldName = event.message

            // ブラックリスト判定
            if (Lib.checkIfContainsBlacklisted(registerWorldName)) {
                event.player.sendMessage("§c使用できない文字列が含まれています。再度入力してください。")
                return
            }

            if (Lib.checkIfAlphaNumeric(registerWorldName)) {
                event.player.sendMessage("§cワールド名には半角英数字のみ使用可能です。再度入力してください。")
                return
            }

            creationDataSet.find { it.player == event.player }!!.worldName = registerWorldName
            creationDataSet.find { it.player == event.player }!!.creationLevel = CreationLevel.CLONE_SOURCE

            event.player.sendMessage("§7ワールドの名前を §a${event.message} §7に設定しました！")
            event.isCancelled = true

            // ソース選択メニュー
            val templateSelectUI = TemplateSelectUI(event.player)
            templateSelectUI.open(true)
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val creationData = creationDataSet.find { it.player == event.whoClicked } ?: return
        val player = event.whoClicked as Player

        // ソース選択
        if (creationData.creationLevel == CreationLevel.CLONE_SOURCE) {
            event.isCancelled = true

            val templateName = (event.currentItem?.itemMeta?.lore?.get(1) ?: return).drop(2)
            val templateWorld = TemplateWorld(templateName)

            // プレビュー開始
            if (event.isLeftClick && !event.isShiftClick) {
                // TODO: プレビュー時に読み込みが挟まないようにしたい
                templateWorld.preview(player)
                player.sendMessage("§e「${templateWorld.name}」§7をプレビュー中...")
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f)
            }

            // 選択
            if (event.isLeftClick && event.isShiftClick) {
                // データを保存
                creationDataSet.find { it.player == player }!!.sourceWorldName = templateName
                creationDataSet.find { it.player == player }!!.creationLevel = CreationLevel.WAITING_CREATION

                // 確認画面に移行（する予定）
                val creationSession = creationDataSet.find { it.player == player }!!

                instance.logger.info("World registered. Player:${creationSession.player.displayName}, World Name: ${creationSession.worldName}, Used Template: ${creationSession.sourceWorldName}")

                creationSession.register()
                creationDataSet.removeIf { it.player == player }
            }
        }

        // 確認表示

    }
}
