package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.custom_item.CustomItem
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object PointCommand: CommandExecutor, TabCompleter {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        if (p0 is Player && p3.isNullOrEmpty()) {
            p0.inventory.addItem(CustomItem.WORLD_PORTAL.item)
        }

        return true
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?,
    ): MutableList<String> {
        return mutableListOf()
    }
}
