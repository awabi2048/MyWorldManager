package me.awabi2048.mw_manager.listener

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.creationDataSet
import me.awabi2048.mw_manager.data_file.Config
import me.awabi2048.mw_manager.my_world.CreationStage.*
import me.awabi2048.mw_manager.my_world.TemplateWorld
import me.awabi2048.mw_manager.ui.ConfirmationUI
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryCloseEvent.Reason
import org.bukkit.event.player.PlayerChatEvent

object WorldCreationSessionListener : Listener {
    @EventHandler
    fun onChatInput(event: PlayerChatEvent) {
        val creationData = creationDataSet.find { it.player == event.player } ?: return
        event.isCancelled = true

        // ワールド名を入力
        if (creationData.creationStage == WORLD_NAME && creationData.worldName == null) {
            val registerWorldName = event.message

            // キャンセル
            if (registerWorldName == Config.cancelFlag) {
                event.player.sendMessage("§c作成をキャンセルしました。")
                creationDataSet.removeIf {it.player == event.player}
                return
            }

            // ブラックリスト判定
            if (Lib.checkIfContainsBlacklisted(registerWorldName)) {
                event.player.sendMessage("§c使用できない文字列が含まれています。再度入力してください。")
                return
            }

            // 文字種判定
            if (!Lib.checkIfAlphaNumeric(registerWorldName)) {
                event.player.sendMessage("§cワールド名には半角英数字のみ使用可能です。再度入力してください。")
                return
            }

            creationDataSet.find { it.player == event.player }!!.worldName = registerWorldName

            // 確認メニュー開く
            val confirmationUI = ConfirmationUI(event.player, ConfirmationUI.UIData.OnCreationName(registerWorldName))
            confirmationUI.open(true)
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val isConfirmationUI = event.view.title == "§8§l確認"
        val creationData = creationDataSet.find { it.player == event.whoClicked } ?: return
        val player = event.whoClicked as Player

        // 確認メニュー
        if (isConfirmationUI) {
            val ui = when(creationData.creationStage) {
                WORLD_NAME -> ConfirmationUI(player, ConfirmationUI.UIData.OnCreationName(creationData.worldName!!))
                CLONE_SOURCE -> ConfirmationUI(player, ConfirmationUI.UIData.OnCreationTemplate(creationData.templateId!!))
                else -> null
            }?: return

            ui.onClick(event)
        }

        // ソース選択
        if (!isConfirmationUI && creationData.creationStage == CLONE_SOURCE) {
            event.isCancelled = true

            val templateId = (event.currentItem?.itemMeta?.lore?.get(1) ?: return).drop(2)
            val templateWorld = TemplateWorld(templateId)

            // プレビュー開始
            if (event.isLeftClick && !event.isShiftClick) {
                // TODO: プレビュー時に読み込みが挟まないようにしたい
                templateWorld.preview(player)
                player.sendMessage("§e「${templateWorld.name}」§7をプレビュー中...")
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f)
            }

            // 選択
            if (event.isLeftClick && event.isShiftClick) {
                // 作成途中データに設定
                creationDataSet.find {it.player == player}!!.templateId = templateId

                // 確認メニュー開く
                val confirmationUI = ConfirmationUI(player, ConfirmationUI.UIData.OnCreationTemplate(templateId))
                confirmationUI.open(true)
            }
        }
    }

    @EventHandler
    fun onInventoryClose (event: InventoryCloseEvent) {
        if (creationDataSet.any {it.player == event.player} && event.reason == Reason.PLAYER) {
            creationDataSet.removeIf {it.player == event.player}
            event.player.sendMessage("§cメニューを閉じたため、作成をキャンセルしました。")
        }
    }
}
