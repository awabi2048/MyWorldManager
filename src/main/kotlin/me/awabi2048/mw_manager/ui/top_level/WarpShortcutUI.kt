package me.awabi2048.mw_manager.ui.top_level

import me.awabi2048.mw_manager.data_file.Config
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.player_data.PlayerData
import me.awabi2048.mw_manager.ui.abstract.AbstractInteractiveUI
import me.awabi2048.mw_manager.ui.children.ConfirmationUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class WarpShortcutUI(private val player: Player) : AbstractInteractiveUI(player) {
    override fun update() {
        val ui = WarpShortcutUI(player)
        ui.open(false)
    }

    override fun onClick(event: InventoryClickEvent) {
        event.isCancelled = true
        if (event.currentItem?.type in listOf(
                Material.GRAY_STAINED_GLASS_PANE,
                Material.BLACK_STAINED_GLASS_PANE
            )
        ) return
        if (event.clickedInventory?.type != InventoryType.CHEST) return

        val itemName = (event.currentItem!!.itemMeta!!.itemName() as TextComponent).content()
        if (itemName == "§cロック中") return

        player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)

        if (event.isLeftClick && !event.isShiftClick) {// 新規登録
            if (itemName == "§b未登録") {
                val world = MyWorldManager.registeredMyWorlds.find { it.vanillaWorld == player.world }
                if (world == null) {

                    player.sendMessage("§cこのワールドではワープを設定できません。")
                    player.playSound(player, Sound.ENTITY_SHULKER_HURT, 1.0f, 1.0f)

                    player.closeInventory()
                    return
                }

                val uuid = world.uuid
                val playerData = PlayerData(player)

                // 登録済
                if (uuid in playerData.warpShortcuts) {
                    player.sendMessage("§c既にこのワールドを登録しています。")
                    player.playSound(player, Sound.ENTITY_SHULKER_HURT, 1.0f, 1.0f)
                    return
                }

                // 確認メニュー
                val confirmationUI = ConfirmationUI(player, ConfirmationUI.UIData.AddWarpShortcut(world))
                confirmationUI.open(true)

            } else { // 登録済み: ワープ

                val uuid = event.currentItem!!.itemMeta!!.lore()!!.map { (it as TextComponent).content() }
                    .find { it.contains("UUID: ") }!!.substringAfter("UUID: ")
                val targetWorld = MyWorld(uuid)

                player.sendMessage("§7登録済みのショートカット先へワープします...")
                targetWorld.warpPlayer(player)
            }

            // 右クリック → 削除
        } else if (event.isRightClick && !event.isShiftClick) {
            if (itemName != "§b未登録") {
                // 確認メニュー
                val uuid =
                    event.currentItem!!.itemMeta!!.lore()!!.map { (it as TextComponent).content() }
                        .find { it.contains("UUID:") }!!.substringAfter("UUID: ")
                val world = MyWorld(uuid)

                val confirmationUI = ConfirmationUI(player, ConfirmationUI.UIData.RemoveWarpShortcut(world))
                confirmationUI.open(true)
            }
        }
    }

    override fun preOpenProcess(firstOpen: Boolean) {
        //
        player.openInventory(ui)
        if (firstOpen) {
            player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1.0f, 2.0f)
        }
    }

    override fun construct(): Inventory {
        fun getOccupiedSlot(uuid: String): ItemStack {
            val world = MyWorld(uuid)

            val icon = world.iconItem!!
            icon.editMeta {
                it.lore(
                    it.lore()!! + listOf(
                        Component.text("§7UUID: $uuid"),
                        Component.text(bar)
                    )
                )
            }

            return icon
        }

        val menu = createTemplate(4, "§8§lワープショートカット")!!

        val playerData = PlayerData(player)

        val lockedIcon = ItemStack(Material.TINTED_GLASS)
        lockedIcon.editMeta {
            it.itemName(Component.text("§cロック中"))
            it.lore(
                listOf(
                    Component.text(bar),
                    Component.text("§7このスロットはアンロックするまで利用できません。"),
                    Component.text(bar),
                )
            )
        }

        val availableIcon = ItemStack(Material.GLASS)
        availableIcon.editMeta {
            it.itemName(Component.text("§b未登録"))
            it.lore(
                listOf(
                    Component.text(bar),
                    Component.text("$index §eクリック §7ワープショートカットに現在のワールドを登録します。"),
                    Component.text(bar),
                )
            )
        }

        fun slotOf(index: Int): Int {
            val row = index / 7
            val column = index % 7
            return 10 + 9 * row + column
        }

        // set
        for (index in 0..<Config.playerWarpSlotMax) menu.setItem(slotOf(index), lockedIcon)
        for (index in 0..<playerData.unlockedWarpSlot) menu.setItem(slotOf(index), availableIcon)

        if (playerData.warpShortcuts.isNotEmpty()) {
            for (index in playerData.warpShortcuts.indices) {
                menu.setItem(slotOf(index), getOccupiedSlot(playerData.warpShortcuts[index]))
            }
        }



        return menu
    }

    override fun onClose(reason: InventoryCloseEvent.Reason) {
    }
}
