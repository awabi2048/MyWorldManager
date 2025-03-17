package me.awabi2048.mw_manager.ui.top_level

import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.world_property.WorldActivityState
import me.awabi2048.mw_manager.ui.abstract.AbstractInteractiveUI
import me.awabi2048.mw_manager.ui.children.ConfirmationUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class AdminWorldInfoUI(val player: Player, private val worlds: Set<MyWorld>, private val page: Int) :
    AbstractInteractiveUI(player) {
    override fun update() {
        val ui = AdminWorldInfoUI(player, worlds, page)
        ui.open(false)
    }

    override fun onClick(event: InventoryClickEvent) {
        event.isCancelled = true

        if (event.slot !in 9..44) return
        if (event.currentItem?.itemMeta?.isHideTooltip == true) return
        val uuid = (event.currentItem?.itemMeta?.lore()?.get(11) as TextComponent?)?.content()?.substringAfter("UUID §b")?: return

        val world = MyWorld(uuid)
        val player = event.whoClicked as Player

        if (event.click.isLeftClick) {
            if (world.activityState == WorldActivityState.ACTIVE) {
                world.warpPlayer(player)
            }
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

            val icon = myWorld.iconItem
            icon?.editMeta { meta ->
                val lore = meta.lore()

                val additionalLore = when(myWorld.activityState) {
                    WorldActivityState.ACTIVE -> listOf(
                        Component.text("$index ${myWorld.activityState!!.japaneseName}"),
                        Component.text("$index §7UUID §b${myWorld.uuid}"),
                        Component.text(bar),
                        Component.text("$index §e左クリック§7 このワールドに§bワープ§7する"),
                        Component.text("$index §e右クリック§7 このワールドのアーカイブ状態を切り替える"),
                        Component.text("$index §eShift + 右クリック§7 このワールドを§c完全に削除§7する"),
                        Component.text(bar),
                    )

                    else -> listOf(
                        Component.text("$index ${myWorld.activityState!!.japaneseName}"),
                        Component.text("$index §7UUID §b${myWorld.uuid}"),
                        Component.text(bar),
                        Component.text("$index §e右クリック§7 このワールドのアーカイブ状態を切り替える"),
                        Component.text("$index §eShift + 右クリック§7 このワールドを§c完全に削除§7する"),
                        Component.text(bar),
                    )
                }

                if (lore != null) {
                    meta.lore(lore + additionalLore)
                }
            }

            val slot = worlds.indexOf(myWorld) % 36 + 9
            ui.setItem(slot, icon)
        }

        return ui
    }

    override fun onClose(reason: InventoryCloseEvent.Reason) {
    }
}
