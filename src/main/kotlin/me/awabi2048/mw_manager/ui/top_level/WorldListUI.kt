package me.awabi2048.mw_manager.ui.top_level

import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.world_property.WorldActivityState
import me.awabi2048.mw_manager.ui.abstract.AbstractInteractiveUI
import net.kyori.adventure.text.Component
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class WorldListUI(val player: Player, val worlds: Collection<MyWorld>, val title: String) :
    AbstractInteractiveUI(player) {

    override fun onClick(event: InventoryClickEvent) {
        if (!(event.slot >= 9 && event.slot % 9 in 1..7)) {
            event.isCancelled = true
            return
        }

        if (event.currentItem?.itemMeta?.isHideTooltip == true) return

        val index = (event.slot / 9 - 1) * 7 + (event.slot % 9 - 1)
        val myWorld = worlds.toList()[index]

        if (event.click.isLeftClick) {
            if (myWorld.activityState == WorldActivityState.ACTIVE) {
                myWorld.warpPlayer(player)
            } else {
                player.closeInventory(InventoryCloseEvent.Reason.PLAYER)
                player.sendMessage("§c§nワールドの自動復帰に失敗したため、ワールドがアーカイブされています。復帰するには、スタッフに報告してください。")
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.5f)
            }
        }
    }

    override fun preOpenProcess(firstOpen: Boolean) {
        if (firstOpen) player.playSound(player, Sound.BLOCK_GRASS_BREAK, 1.0f, 1.0f)
    }

    override fun construct(): Inventory {
        val uiRow = (worlds.size - 1) / 7 + 3

        val ui = createTemplate(uiRow, title)!!

        worlds.forEach { world ->
            val icon = world.iconItem!!
            icon.editMeta {
                val lore = it.lore()!!
                lore += listOf(
                    Component.text("$index §7クリック §fこのワールドに§bワープ"),
                    Component.text(bar)
                )

                it.lore(lore)
            }

            val index = worlds.indexOf(world)
            val row = index / 7 + 1
            val column = index % 7 + 1
            val slot = row * 9 + column

            ui.setItem(slot, icon)
        }

        return ui
    }

    override fun onClose(reason: InventoryCloseEvent.Reason) {
    }

    override fun update() {
        val ui = WorldListUI(player, worlds, title)
        ui.open(false)
    }
}
