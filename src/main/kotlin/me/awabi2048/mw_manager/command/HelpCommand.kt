package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.data_file.Config
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object HelpCommand: CommandExecutor {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        if (!p3.isNullOrEmpty()) {
            p0.sendMessage("§c無効なコマンドです。")
            return true
        }

        if (!CommandManager.hasCorrectPermission(p0, this)) {
            p0.sendMessage("§c権限がありません。")
            return true
        }

        p0.sendMessage("§7----------------------")
        for (command in Config.commandHelp!!.filter {p0.hasPermission("mw_manager.command.${it.key}")}) {
            p0.sendMessage("${command.key}: ${command.value}")
        }
        p0.sendMessage("§7----------------------")

        return true
    }
}
