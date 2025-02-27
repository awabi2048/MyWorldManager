package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.command.Option.*
import me.awabi2048.mw_manager.player_expansion.notify
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

object MWMCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty() || args[0] !in Option.entries.map { it.toString().lowercase() }) {
            sender.notify("§c無効なコマンドです。", null)
            return true
        }

        val option = Option.valueOf(args[0].uppercase())
        val subcommandExecutor = MWMSubCommand(sender, args)

        when (option) {
            CREATE -> subcommandExecutor.create()
            INFO -> subcommandExecutor.info()
            DEACTIVATE -> TODO()
            ACTIVATE -> TODO()
            UPDATE -> subcommandExecutor.update()
            START_CREATION_SESSION -> subcommandExecutor.startCreationSession()
            RELOAD -> subcommandExecutor.reload()
        }

        return true
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?,
    ): MutableList<String> {
        return CommandManager.getTabCompletion(p3?.toList(), this)
    }
}
