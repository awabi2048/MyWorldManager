package me.awabi2048.mw_manager.ui

import me.awabi2048.mw_manager.EmojiIcon
import me.awabi2048.mw_manager.my_world.ExpandMethod
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.player_data.PlayerData
import me.awabi2048.mw_manager.player_notification.PlayerNotification
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class WorldExpandUI(val owner: Player, val world: MyWorld) : AbstractInteractiveUI(owner) {
    init {
        if (!world.isRegistered) {
            throw IllegalStateException("Unregistered world given.")
        }
    }

    override fun onClick(event: InventoryClickEvent) {
        if (event.slot !in listOf(11, 13, 15, 17)) return

        val expandMethod = when (event.slot) {
            11 -> ExpandMethod.LEFT_UP
            13 -> ExpandMethod.LEFT_DOWN
            15 -> ExpandMethod.RIGHT_DOWN
            17 -> ExpandMethod.RIGHT_UP
            else -> return
        }

        owner.playSound(owner, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)

        // コスト確認
        val cost = world.expansionCost!!
        val playerData = PlayerData(owner)
        val playerPoint = playerData.worldPoint
        if (playerPoint < cost) {
            PlayerNotification.NOT_ENOUGH_WORLD_POINT.send(owner)
            owner.closeInventory()

            return
        }

        // 拡張を実行
        world.expand(expandMethod)

        PlayerNotification.WORLD_EXPANSION_SUCCEEDED.send(owner)
        owner.sendMessage("§8【 §7${world.borderExpansionLevel!! - 1} §f▶ §e§l${world.borderExpansionLevel} §8】 §7(残り ${EmojiIcon.WORLD_POINT} §e${playerData.worldPoint}§7)")
    }

    override fun open() {
        owner.openInventory(ui)
    }

    override fun construct(): Inventory {
        val ui = createTemplate(3, "§8§lExpand")!!

        // 左上
        val methodLeftUp = ItemStack(Material.CHEST)
        methodLeftUp.editMeta {
            it.itemName(Component.text("左上に拡張").color(AQUA))
            it.lore(
                mutableListOf(
                    Component.text(bar),
                    Component.text("§7ワールドを左上(北西)方向に拡張します。"),
                    Component.text(bar),
                    Component.text("§7██"),
                    Component.text("§7█§b█"),
                    Component.text("$index §7灰色: 拡張されるエリア"),
                    Component.text("$index §7水色: 現在のエリア"),
                    Component.text(bar),
                )
            )
        }

        // 左下
        val methodLeftDown = ItemStack(Material.CHEST)
        methodLeftDown.editMeta {
            it.itemName(Component.text("左上に拡張").color(AQUA))
            it.lore(
                mutableListOf(
                    Component.text(bar),
                    Component.text("§7ワールドを左下(南西)方向に拡張します。"),
                    Component.text(bar),
                    Component.text("§7█§b█"),
                    Component.text("§7██"),
                    Component.text("$index §7灰色: 拡張されるエリア"),
                    Component.text("$index §7水色: 現在のエリア"),
                    Component.text(bar),
                )
            )
        }

        // 右下
        val methodRightDown = ItemStack(Material.CHEST)
        methodRightDown.editMeta {
            it.itemName(Component.text("左上に拡張").color(AQUA))
            it.lore(
                mutableListOf(
                    Component.text(bar),
                    Component.text("§7ワールドを右下(南東)方向に拡張します。"),
                    Component.text(bar),
                    Component.text("§b█§7█"),
                    Component.text("§7██"),
                    Component.text("$index §7灰色: 拡張されるエリア"),
                    Component.text("$index §7水色: 現在のエリア"),
                    Component.text(bar),
                )
            )
        }

        // 右上
        val methodRightUp = ItemStack(Material.CHEST)
        methodRightUp.editMeta {
            it.itemName(Component.text("右上に拡張").color(AQUA))
            it.lore(
                mutableListOf(
                    Component.text(bar),
                    Component.text("§7ワールドを右上(北東)方向に拡張します。"),
                    Component.text(bar),
                    Component.text("§7██"),
                    Component.text("§b█§7█"),
                    Component.text("$index §7灰色: 拡張されるエリア"),
                    Component.text("$index §7水色: 現在のエリア"),
                    Component.text(bar),
                )
            )
        }

        // set
        ui.setItem(10, methodLeftUp)
        ui.setItem(12, methodLeftDown)
        ui.setItem(14, methodRightDown)
        ui.setItem(16, methodRightUp)

        return ui
    }
}
