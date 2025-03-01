package me.awabi2048.mw_manager.listener

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent
import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.confirmationTracker
import me.awabi2048.mw_manager.Main.Companion.creationDataSet
import me.awabi2048.mw_manager.data_file.Config
import me.awabi2048.mw_manager.my_world.CreationStage.*
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.my_world.TemplateWorld
import me.awabi2048.mw_manager.ui.ConfirmationUI
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryCloseEvent.Reason
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.player.PlayerLoginEvent

object WorldCreationSessionListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
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

            // 各種判定
            if (!Lib.checkWorldNameAvailable(registerWorldName, event.player)) {
                return
            }

            creationDataSet.find { it.player == event.player }!!.worldName = registerWorldName

            // 確認メニュー開く
            val confirmationUI = ConfirmationUI(event.player, ConfirmationUI.UIData.OnCreationName(registerWorldName))
            event.player.playSound(event.player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
            confirmationUI.open(true)
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val creationData = creationDataSet.find { it.player == event.whoClicked } ?: return
        val player = event.whoClicked as Player

        // ソース選択
        if (creationData.creationStage == CLONE_SOURCE && !confirmationTracker.any {it.player == player}) {
            event.isCancelled = true

            val templateId = (event.currentItem?.itemMeta?.lore?.get(1) ?: return).drop(2)
            val templateWorld = TemplateWorld(templateId)

            player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)

            // プレビュー開始
            if (event.isLeftClick && !event.isShiftClick) {
                // TODO: プレビュー時に読み込みが挟まないようにしたい
                templateWorld.preview(player)
                player.closeInventory()

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
        if (creationDataSet.any {it.player == event.player} && event.reason == Reason.PLAYER && event.inventory.type == InventoryType.CHEST) {
            val player = event.player as Player

            creationDataSet.removeIf {it.player == player}
            player.sendMessage("§cメニューを閉じたため、作成をキャンセルしました。")
            player.playSound(player, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 0.5f)
        }
    }

    // プレビュー、スペクテイター外れようとしたとき
    @EventHandler
    fun onPlayerEscape(event: PlayerStopSpectatingEntityEvent) {
        if (event.spectatorTarget.scoreboardTags.contains("mwm.template_preview")) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        // テンプレートワールドの中にいたら追い出す
        if (MyWorldManager.registeredTemplateWorld.any {it.cbWorld == event.player.world}) {
            val escapeLocation = Config.escapeLocation?: return

            event.player.teleport(escapeLocation)
            if (event.player.gameMode == GameMode.SPECTATOR) event.player.gameMode = event.player.previousGameMode?: GameMode.SURVIVAL
            event.player.playSound(event.player, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 2.0f)
        }
    }
}
