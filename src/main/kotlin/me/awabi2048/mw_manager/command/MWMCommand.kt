package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.Main
import me.awabi2048.mw_manager.Main.Companion.prefix
import me.awabi2048.mw_manager.player_notify.notify
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

object MWMCommand: CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty() || args[0] !in listOf("create", "info", "deactivate", "activate")) {
            sender.notify("§c無効なコマンドです。", null)
            return true
        }

        val option = Option.valueOf(args[0].uppercase())



    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?
    ): MutableList<String>? {
        TODO("Not yet implemented")
    }
}