package me.awabi2048.mw_manager.command.command_executor

import me.awabi2048.mw_manager.command.CommandManager
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.ui.top_level.WorldManagementUI
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object OpenWorldUICommand : CommandExecutor, TabCompleter {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        if (p0 !is Player) {
            p0.sendMessage("MVM >> This command is only available on player execution.")
            return true
        }

        if (!CommandManager.hasCorrectPermission(p0, this)) {
            p0.sendMessage("§c権限がありません。")
            return true
        }

        if (!p3.isNullOrEmpty()) {
            p0.sendMessage("§c無効なコマンドです。")
            return true
        }

        val world = MyWorldManager.registeredMyWorlds.find { it.vanillaWorld == p0.world }
        if (world == null) {
            p0.sendMessage("§cこのワールドでは使用できないコマンドです。")
            return true
        }

        if (p0 !in world.players!!) {
            p0.sendMessage("§cメニューを開くには、ワールドのメンバーである必要があります。")
            return true
        }

        val ui = WorldManagementUI(p0, world)
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
