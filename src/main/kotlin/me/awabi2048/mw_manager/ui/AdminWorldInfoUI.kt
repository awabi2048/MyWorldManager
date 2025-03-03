package me.awabi2048.mw_manager.ui

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.my_world.MyWorld
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class AdminWorldInfoUI(val player: Player, private val worlds: Set<MyWorld>, private val page: Int) : AbstractInteractiveUI(player) {
    override fun update() {
        val ui = AdminWorldInfoUI(player, worlds, page)
        ui.open(false)
    }

    override fun onClick(event: InventoryClickEvent) {
        event.isCancelled = true

        if (event.slot !in 9..44) return
        if (event.currentItem?.itemMeta?.isHideTooltip == true) return
        val ownerName = (event.currentItem!!.itemMeta.lore()!![2] as TextComponent).content().drop(15)
        val worldName = (event.currentItem!!.itemMeta.itemName() as TextComponent).content().drop(5).dropLast(3)

        val world = Lib.translateWorldSpecifier("$ownerName:$worldName")?: return
        val player = event.whoClicked as Player

        if (event.click.isLeftClick) {
            world.warpPlayer(player, false)
            return
        }

        if (event.click.isRightClick && event.click.isShiftClick) {
            val ui = ConfirmationUI(player, ConfirmationUI.UIData.WorldAdminDelete(world))
            ui.open(true)
            return
        }

        if (event.click.isRightClick && !event.click.isShiftClick) {
            val ui = ConfirmationUI(player, ConfirmationUI.UIData.WorldAdminToggleActivity(world))
            ui.open(true)
            return
        }
    }

    override fun preOpenProcess(firstOpen: Boolean) {
        if (firstOpen) player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 1.8f)
    }

    override fun construct(): Inventory {
        val ui = createTemplate(6, "§8§lワールドデータの管理")!!
        val displayRange = (page - 1) * 36..<page * 36

        // 与えられたリストからアイテムを配置していく
        worlds.filter { worlds.indexOf(it) in displayRange }.forEach { myWorld ->
//            println(myWorld.iconMaterial)

            val icon = ItemStack(myWorld.iconMaterial!!)

            icon.editMeta { meta ->
                meta.itemName(Component.text("§8【§a${myWorld.name}§8】"))
                meta.lore(
                    myWorld.fixedData.map { Component.text(it) } + listOf(
                        Component.text("$index §7UUID §b${myWorld.uuid}"),
                        Component.text(bar),
                        Component.text("$index §7ステータス ${myWorld.activityState?.toJapanese()}"),
                        Component.text(bar),
                        Component.text("$index §e左クリック§7 このワールドに§bワープ§7する"),
                        Component.text("$index §e右クリック§7 このワールドのアーカイブ状態を切り替える"),
                        Component.text("$index §eShift + 右クリック§7 このワールドを§c完全に削除§7する"),
                        Component.text(bar),
                    )
                )

                if (myWorld.isOutDated!!) meta.setEnchantmentGlintOverride(true)
            }

            val slot = worlds.indexOf(myWorld) % 36 + 9
            ui.setItem(slot, icon)
        }

        return ui
    }

    override fun onClose(reason: InventoryCloseEvent.Reason) {
    }
}
