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
        val subcommandExecutor = SubCommand(sender, args)

        when (option) {
            CREATE -> subcommandExecutor.create()
            INFO -> TODO()
            DEACTIVATE -> TODO()
            ACTIVATE -> TODO()
            UPDATE -> subcommandExecutor.update()
            START_CREATION_SESSION -> subcommandExecutor.startCreationSession()
        }

        return true
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?,
    ): List<String> {
        if (p3.isNullOrEmpty()) return Option.entries.map { it.toString().lowercase() }

        if (p3[0] == "create") {

            if (p3.size == 1) return listOf("player:", "puuid:")
            if (p3.size == 2) {
                if (p3[1].startsWith("player:")) return listOf()
                if (p3[1].startsWith("puuid:")) return listOf()
            }
            if (p3.size == 3) {
                if (p3[2].startsWith("world:")) return MyWorldManager.registeredWorld.map { it.name!! }
                if (p3[2].startsWith("wuuid:")) return MyWorldManager.registeredWorld.map { it.uuid }
            }
        }

        return listOf()
    }
}
