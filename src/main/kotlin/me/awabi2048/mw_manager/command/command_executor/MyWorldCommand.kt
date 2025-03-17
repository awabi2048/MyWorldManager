package me.awabi2048.mw_manager.command.command_executor

import me.awabi2048.mw_manager.command.CommandManager
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.ui.top_level.WorldListUI
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object MyWorldCommand: CommandExecutor, TabCompleter {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        if (p0 !is Player) {
            p0.sendMessage("§cThis command is only available with player execution.")
            return true
        }

        if (!p3.isNullOrEmpty()) {
            p0.sendMessage("§c無効なコマンドです。 /myworld")
            return true
        }

        if (!MyWorldManager.registeredMyWorlds.any {p0 in it.members!!}) {
            p0.sendMessage("§7表示できるワールドがありませんでした。")
            p0.playSound(p0, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
            return true
        }

        val worlds = MyWorldManager.registeredMyWorlds.filter {p0 in it.members!!}
        val ui = WorldListUI(p0, worlds, "§8§lあなたのワールド")
        ui.open(true)
        return true
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?,
    ): MutableList<String> {
        return CommandManager.getTabCompletion(p0, p3?.toList(), this)
    }
}
