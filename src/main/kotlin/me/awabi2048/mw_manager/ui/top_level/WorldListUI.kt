package me.awabi2048.mw_manager.ui.top_level

import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.ui.abstract.AbstractInteractiveUI
import net.kyori.adventure.text.Component
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class WorldListUI(val player: Player, val worlds: Collection<MyWorld>, val title: String) : AbstractInteractiveUI(player) {

    private val availableWorlds = MyWorldManager.registeredMyWorlds.filter {player in it.players!!}

    override fun onClick(event: InventoryClickEvent) {
        if (!(event.slot >= 9 && event.slot % 9 in 1..7)) {
            event.isCancelled = true
            return
        }

        val index = (event.slot / 9 - 1) * 7 + (event.slot % 9 - 1)
        val myWorld = availableWorlds[index]

        if (event.click.isLeftClick) {
            myWorld.warpPlayer(player, true)
        }
    }

    override fun preOpenProcess(firstOpen: Boolean) {
        if (firstOpen) player.playSound(player, Sound.BLOCK_GRASS_BREAK, 1.0f, 1.0f)
    }

    override fun construct(): Inventory {
        val uiRow = (availableWorlds.size - 1) / 7 + 3

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
