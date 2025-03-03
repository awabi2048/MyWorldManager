package me.awabi2048.mw_manager.listener

import me.awabi2048.mw_manager.Main.Companion.playerUIState
import me.awabi2048.mw_manager.Main.Companion.worldSettingState
import me.awabi2048.mw_manager.ui.PlayerWorldSettingState
import me.awabi2048.mw_manager.ui.WorldManagementUI
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.player.PlayerInteractEvent

object WorldSettingListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onChatInput(event: PlayerChatEvent) {
        if (worldSettingState[event.player] !in listOf(
                PlayerWorldSettingState.CHANGE_NAME,
                PlayerWorldSettingState.CHANGE_DESCRIPTION,
                PlayerWorldSettingState.ADD_MEMBER
            )
        ) return

        event.isCancelled = true
        val ui = playerUIState[event.player] as WorldManagementUI? ?: return
        ui.setByChatInput(event.message)
    }

    @EventHandler
    fun onPlayerClickBlock(event: PlayerInteractEvent) {
        if (event.action !in listOf(Action.RIGHT_CLICK_BLOCK, Action.LEFT_CLICK_BLOCK)) return
        if (worldSettingState[event.player] !in listOf(
                PlayerWorldSettingState.CHANGE_MEMBER_SPAWN_POS,
                PlayerWorldSettingState.CHANGE_GUEST_SPAWN_POS
            )
        ) return

        event.isCancelled = true

        val location = event.clickedBlock?.location?.toBlockLocation()?.add(0.0, 1.0, 0.0) ?: return

        val ui = playerUIState[event.player] as WorldManagementUI? ?: return
        ui.setSpawnLocation(location)
    }

    @EventHandler
    fun onPlayerClickInventory(event: InventoryClickEvent) {
        // アイコン変更: 最後の条件はメニュー内のアイテムを選択できないようにするため
//        if (worldSettingState[event.whoClicked] == PlayerWorldSettingState.CHANGE_ICON && event.currentItem != null && !event.clickedInventory!!.any{it != null && it.itemMeta.itemName == "§aワールド表示の変更"}) {
//            event.isCancelled = true
//            val player = event.whoClicked as Player
//
//            val myWorld = MyWorldManager.registeredMyWorld.find {it.vanillaWorld == player.world}?: return
//            myWorld.iconMaterial = event.currentItem!!.type
//
//            // アイテム名を翻訳したいのでComponentを使用
//            val message = Component.text("§7ワールドのアイコンを")
//                .append(Component.translatable(event.currentItem!!.type.translationKey()).color(AQUA))
//                .append(Component.text("§7に変更しました。"))
//
//            player.sendMessage(message)
//            player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
//            player.playSound(player, Sound.BLOCK_ANVIL_LAND, 1.0f, 2.0f)
//
//            worldSettingState.remove(player)
//            val ui = WorldManagementUI(player, myWorld)
//            ui.open(false)
//
//            return
//        }

        // ワールド設定メニュー
//        if (event.clickedInventory?.any { it != null && it.itemMeta?.itemName == "§aワールド表示の変更" } == true) {
//
//            event.isCancelled = true
//
//            val player = event.whoClicked as Player
//            val world = MyWorldManager.registeredMyWorld.find { it.vanillaWorld == player.world } ?: return
//
//            val menu = WorldManagementUI(player, world)
//            menu.onClick(event)
//            return
//        }

        // ワールド拡張メニュー
//        if (event.clickedInventory?.any { it != null && it.itemMeta?.itemName() == Component.text("左上に拡張").color(AQUA) } == true) {
//
//            event.isCancelled = true
//
//            val player = event.whoClicked as Player
//            val world = MyWorldManager.registeredMyWorld.find { it.vanillaWorld == player.world } ?: return
//
//            val menu = WorldExpandUI(player, world)
//            menu.onClick(event)
//            return
//        }
    }
}
