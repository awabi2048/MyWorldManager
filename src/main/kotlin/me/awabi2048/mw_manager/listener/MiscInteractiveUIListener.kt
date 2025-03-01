package me.awabi2048.mw_manager.listener

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.confirmationTracker
import me.awabi2048.mw_manager.ui.ConfirmationUI
import me.awabi2048.mw_manager.ui.WarpShortcutUI
import me.awabi2048.mw_manager.ui.WorldInfoListUI
import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

object MiscInteractiveUIListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.view.title == "§8§lワープショートカット") {
            val ui = WarpShortcutUI(event.whoClicked as Player)
            ui.onClick(event)
        }
    }

    @EventHandler
    fun onConfirmationUIClick(event: InventoryClickEvent) {
        if (event.view.title != "§8§l確認") return
        val tracker = confirmationTracker.find {it.player == event.whoClicked}?: return
        ConfirmationUI(tracker.player, tracker.uiData).onClick(event)
    }

    @EventHandler
    fun onAdminInfoUIClick(event: InventoryClickEvent) {
        if (event.view.title != "§8§lワールドデータの管理") return

        event.isCancelled = true

        if (event.slot !in 9..44) return
        if (event.currentItem?.itemMeta?.isHideTooltip == true) return
        val ownerName = (event.currentItem!!.itemMeta.lore()!![2] as TextComponent).content().drop(15)
        val worldName = (event.currentItem!!.itemMeta.itemName() as TextComponent).content().drop(5).dropLast(3)

        val world = Lib.translateWorldSpecifier("$ownerName:$worldName")?: return
        val player = event.whoClicked as Player

        if (event.click.isLeftClick) {
            world.warpPlayer(player)
        }

        if (event.click.isRightClick && event.click.isShiftClick) {
            val ui = ConfirmationUI(player, ConfirmationUI.UIData.WorldAdminDelete(world))
            ui.open(true)
        }

        if (event.click.isRightClick && !event.click.isShiftClick) {
            val ui = ConfirmationUI(player, ConfirmationUI.UIData.WorldAdminToggleActivity(world))
            ui.open(true)
        }
    }
}
