package me.awabi2048.mw_manager.ui.top_level

import me.awabi2048.mw_manager.portal.PortalColor
import me.awabi2048.mw_manager.portal.WorldPortal
import me.awabi2048.mw_manager.ui.abstract.AbstractInteractiveUI
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class PortalUI(private val player: Player, private val portal: WorldPortal) : AbstractInteractiveUI(player) {
    override fun update() {
        val ui = PortalUI(player, portal)
        ui.open(false)
    }

    override fun onClick(event: InventoryClickEvent) {
        val slot = event.slot
        if (slot !in listOf(11, 13, 15)) {
            event.isCancelled = true
            return
        }

        player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)

        // テキスト表示
        if (slot == 11) {
            portal.displayText = !portal.displayText
            update()
        }

        // 色
        if (slot == 13) {
            val index = when (event.click.isLeftClick) {
                true -> portal.color.ordinal + 1
                false -> portal.color.ordinal - 1
            }.coerceIn(0..<PortalColor.entries.size)

            portal.color = PortalColor.entries[index]
            update()
        }

        // 撤去
        if (slot == 15) {
            player.closeInventory(InventoryCloseEvent.Reason.CANT_USE)
            portal.remove(true)
        }
    }

    override fun preOpenProcess(firstOpen: Boolean) {
        if (firstOpen) {
            player.playSound(player, Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0f, 0.5f)
        }
    }

    override fun construct(): Inventory {
        val ui = createTemplate(3, "$titleBar §8§lポータルの管理 $titleBar")

        // テキスト表示の切り替え
        val textToggleIcon = ItemStack(Material.NAME_TAG)
        textToggleIcon.editMeta {
            val displayState = when (portal.displayText) {
                true -> "§a有効"
                false -> "§c無効"
            }

            it.itemName(Component.text("§bテキスト表示の切り替え"))
            it.lore(
                listOf(
                    Component.text(bar),
                    Component.text("$index §7クリック §e転送先の表示§fを切り替えます。"),
                    Component.text("$index §7現在の設定 $displayState"),
                    Component.text(bar),
                )
            )
        }

        // 色の変更
        val colorMaterial = Material.valueOf("${portal.color}_WOOL")
        val colorIcon = ItemStack(colorMaterial)
        colorIcon.editMeta {
            it.itemName(Component.text("§bパーティクルの色"))
            it.lore(
                listOf(
                    Component.text(bar),
                    Component.text("$index §7クリック §eポータルのパーティクルの色§7を変更します。"),
                    Component.text("$index §7現在の設定 ${portal.color.japaneseName}"),
                    Component.text(bar),
                )
            )
        }

        // 撤去
        val removeIcon = ItemStack(Material.BEDROCK)
        removeIcon.editMeta {
            it.itemName(Component.text("§cポータルを撤去"))
            it.lore(
                listOf(
                    Component.text(bar),
                    Component.text("$index §7クリック §7ポータルを撤去します。"),
                    Component.text(bar),
                )
            )
        }

        //
        ui.setItem(11, textToggleIcon)
        ui.setItem(13, colorIcon)
        ui.setItem(15, removeIcon)

        return ui
    }

    override fun onClose(reason: InventoryCloseEvent.Reason) {
    }

}
