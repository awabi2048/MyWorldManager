package me.awabi2048.mw_manager.listener

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.worldSettingState
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.player_notification.PlayerNotification
import me.awabi2048.mw_manager.ui.PlayerWorldSettingState
import me.awabi2048.mw_manager.ui.WorldExpandUI
import me.awabi2048.mw_manager.ui.WorldManagementUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.player.PlayerInteractEvent

object WorldSettingListener : Listener {
    @EventHandler
    fun onChatInput(event: PlayerChatEvent) {
        if (worldSettingState[event.player] !in listOf(
                PlayerWorldSettingState.CHANGE_NAME,
                PlayerWorldSettingState.CHANGE_DESCRIPTION,
                PlayerWorldSettingState.ADD_MEMBER
            )
        ) return

        event.isCancelled = true

        val state = worldSettingState[event.player]!!
        val world = MyWorldManager.registeredMyWorld.find { it.vanillaWorld == event.player.world }
        if (world == null) {
            PlayerNotification.MISC_ERROR.send(event.player)
            return
        }

        if (state == PlayerWorldSettingState.CHANGE_NAME || state == PlayerWorldSettingState.CHANGE_DESCRIPTION) {

            val text = event.message

            if (state == PlayerWorldSettingState.CHANGE_NAME) {
                if (!Lib.checkWorldNameAvailable(text, event.player)) return

                world.name = text
                event.player.sendMessage("§eワールドの名前が §6${text} §eに変更されました！")
            }

            if (state == PlayerWorldSettingState.CHANGE_DESCRIPTION) {
                // ブラックリスト判定
                if (Lib.checkIfContainsBlacklisted(text)) {
                    event.player.sendMessage("§c使用できない文字列が含まれています。再度入力してください。")
                    return
                }

                world.description = text
                event.player.sendMessage("§eワールドの説明が §6${text} §eに変更されました！")
            }

        } else {
            val playerName = event.message
            val player = Bukkit.getPlayer(playerName)

            if (player == null || !player.isOnline) {
                PlayerNotification.INVALID_PLAYER_GIVEN.send(event.player)
                return
            }

            world.recruitPlayer(event.player, player)
        }

        worldSettingState.remove(event.player)
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

        val state = worldSettingState[event.player]!!

        val location = event.clickedBlock?.location?.toBlockLocation()?.add(0.0, 1.0, 0.0) ?: return

        val world = MyWorldManager.registeredMyWorld.find { it.vanillaWorld == event.player.world }
        if (world == null) {
            PlayerNotification.MISC_ERROR.send(event.player)
            return
        }

        when (state) {
            PlayerWorldSettingState.CHANGE_GUEST_SPAWN_POS -> {
                event.player.sendMessage("§7ゲストのワールドスポーン位置が (§b${location.blockX}§7, §b${location.blockY}§7, §b${location.blockZ}§7)に変更されました。")
                world.guestSpawnLocation = location
            }
            PlayerWorldSettingState.CHANGE_MEMBER_SPAWN_POS -> {
                event.player.sendMessage("§7メンバーのワールドスポーン位置が (§b${location.blockX}§7, §b${location.blockY}§7, §b${location.blockZ}§7)に変更されました。")
                world.memberSpawnLocation = location
            }
            else -> return
        }

        worldSettingState.remove(event.player)
    }

    @EventHandler
    fun onPlayerClickInventory(event: InventoryClickEvent) {
        // アイコン変更: 最後の条件はメニュー内のアイテムを選択できないようにするため
        if (worldSettingState[event.whoClicked] == PlayerWorldSettingState.CHANGE_ICON && event.currentItem != null && !event.clickedInventory!!.any{it != null && it.itemMeta.itemName == "§aワールド表示の変更"}) {
            event.isCancelled = true
            val player = event.whoClicked as Player

            val myWorld = MyWorldManager.registeredMyWorld.find {it.vanillaWorld == player.world}?: return
            myWorld.iconMaterial = event.currentItem!!.type

            // アイテム名を翻訳したいのでComponentを使用
            val message = Component.text("§7ワールドのアイコンを")
                .append(Component.translatable(event.currentItem!!.type.translationKey()).color(AQUA))
                .append(Component.text("§7に変更しました。"))

            player.sendMessage(message)
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
            player.playSound(player, Sound.BLOCK_ANVIL_LAND, 1.0f, 2.0f)

            worldSettingState.remove(player)
            val ui = WorldManagementUI(player, myWorld)
            ui.open(false)

            return
        }

        // ワールド設定メニュー
        if (event.clickedInventory?.any { it != null && it.itemMeta?.itemName == "§aワールド表示の変更" } == true) {

            event.isCancelled = true

            val player = event.whoClicked as Player
            val world = MyWorldManager.registeredMyWorld.find { it.vanillaWorld == player.world } ?: return

            val menu = WorldManagementUI(player, world)
            menu.onClick(event)
            return
        }

        // ワールド拡張メニュー
        if (event.clickedInventory?.any { it != null && it.itemMeta?.itemName() == Component.text("左上に拡張").color(AQUA) } == true) {

            event.isCancelled = true

            val player = event.whoClicked as Player
            val world = MyWorldManager.registeredMyWorld.find { it.vanillaWorld == player.world } ?: return

            val menu = WorldExpandUI(player, world)
            menu.onClick(event)
            return
        }
    }

    @EventHandler
    fun onPlayerCloseInventory(event: InventoryCloseEvent) {
        if (worldSettingState[event.player] == PlayerWorldSettingState.CHANGE_ICON) {
            worldSettingState.remove(event.player)
            event.player.sendMessage("§cインベントリを閉じたため、設定をキャンセルしました。")
        }
    }

    @EventHandler
    fun onPlayerChangeWorld(event: PlayerChangedWorldEvent) {
        if (worldSettingState[event.player] != null) {
            worldSettingState.remove(event.player)
            event.player.sendMessage("§cワールドを移動したため、設定をキャンセルしました。")
        }
    }
}
