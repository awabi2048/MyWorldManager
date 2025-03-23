package me.awabi2048.mw_manager.command.command_executor

import me.awabi2048.mw_manager.command.CommandManager
import me.awabi2048.mw_manager.command.MWMSubCommand
import me.awabi2048.mw_manager.command.Option
import me.awabi2048.mw_manager.command.Option.*
import me.awabi2048.mw_manager.my_world.world_property.WorldActivityState
import me.awabi2048.mw_manager.extension.notify
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

object MWMCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!CommandManager.hasCorrectPermission(sender, this)) {
            sender.sendMessage("§c権限がありません。")
            return true
        }

        if (args.isEmpty() || args[0] !in Option.entries.map { it.toString().lowercase() }) {
            sender.notify("§c無効なコマンドです。 /mwm <オプション>", null)
            return true
        }

        val option = Option.valueOf(args[0].uppercase())
        val subcommandExecutor = MWMSubCommand(sender, args)

        when (option) {
            CREATE -> subcommandExecutor.create()
            INFO -> subcommandExecutor.info()
            ARCHIVE -> subcommandExecutor.setWorldState(WorldActivityState.ARCHIVED)
            ACTIVATE -> subcommandExecutor.setWorldState(WorldActivityState.ACTIVE)
            UPDATE -> subcommandExecutor.update()
            START_CREATION_SESSION -> subcommandExecutor.startCreationSession()
            RELOAD -> subcommandExecutor.reload()
            GET_ITEM -> subcommandExecutor.getItem()
            PLAYER_DATA -> subcommandExecutor.modifyPlayerData()
            SETUP_TEMPLATE -> subcommandExecutor.setupTemplate()
        }

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
