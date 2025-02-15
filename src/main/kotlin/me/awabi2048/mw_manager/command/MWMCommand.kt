package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.Main
import me.awabi2048.mw_manager.Main.Companion.mvWorldManager
import me.awabi2048.mw_manager.Main.Companion.prefix
import me.awabi2048.mw_manager.Main.Companion.registeredWorldData
import me.awabi2048.mw_manager.MyWorld
import me.awabi2048.mw_manager.MyWorldManager
import me.awabi2048.mw_manager.command.Option.*
import me.awabi2048.mw_manager.player_notify.notify
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

object MWMCommand: CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty() || args[0] !in listOf("create", "info", "deactivate", "activate", "update")) {
            sender.notify("§c無効なコマンドです。", null)
            return true
        }

        val option = Option.valueOf(args[0].uppercase())
        when(option) {
            CREATE -> SubCommand(sender, args).create()
            INFO -> TODO()
            DEACTIVATE -> TODO()
            ACTIVATE -> TODO()
            UPDATE -> SubCommand(sender, args).update()
        }

        return true
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?
    ): List<String> {
        if (p3.isNullOrEmpty()) return listOf("create", "info", "deactivate", "activate")

        if (p3[0] == "create") {

            if (p3.size == 1) return listOf("player:", "puuid:")
            if (p3.size == 2) {
                if (p3[1].startsWith("player:")) return listOf()
                if (p3[1].startsWith("puuid:")) return listOf()
            }
            if (p3.size == 3) {
                if (p3[2].startsWith("world:")) return MyWorldManager.registeredWorld.map{it.name!!}
                if (p3[2].startsWith("wuuid:")) return MyWorldManager.registeredWorld.map{it.uuid}
            }
        }

        return listOf()
    }
}
