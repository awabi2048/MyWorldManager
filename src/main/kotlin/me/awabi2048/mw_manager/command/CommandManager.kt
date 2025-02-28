package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.my_world.MyWorldManager
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor

object CommandManager {
    fun setExecutor() {
        instance.getCommand("myworldmanager")?.setExecutor(MWMCommand)
        instance.getCommand("mwmanager")?.setExecutor(MWMCommand)
        instance.getCommand("mwm")?.setExecutor(MWMCommand)

        instance.getCommand("invite")?.setExecutor(InviteCommand)
        instance.getCommand("visit")?.setExecutor(VisitCommand)

        instance.getCommand("worldpoint")?.setExecutor(WorldPointCommand)
        instance.getCommand("mwm_invite_accept")?.setExecutor(InviteAcceptCommand)

        instance.getCommand("worldmenu")?.setExecutor(OpenWorldUICommand)
        instance.getCommand("worldwarp")?.setExecutor(WarpCommand)
    }

    fun getTabCompletion(args: List<String>?, executor: CommandExecutor): MutableList<String> {
        fun worldSpecifier(arg: String): MutableList<String> {
            if (!arg.contains(":")) {
                return Bukkit.getOnlinePlayers().map { "${it.displayName}:" }.toMutableList()
            } else {
                val playerName = arg.substringBefore(":")
                val playerUUID = Bukkit.getPlayer(playerName)?.uniqueId ?: return mutableListOf()
                return MyWorldManager.registeredMyWorld.filter { it.owner!!.uniqueId == playerUUID }
                    .map { "$playerName:${it.name!!}" }.toMutableList()
            }
        }

        fun playerSpecifier(arg: String): MutableList<String> {
            if (!arg.contains(":")) {
                return mutableListOf("player:", "uuid:")
            } else {
                if (arg.contains("player:")) return Bukkit.getOnlinePlayers().map {"player:${it.displayName}"}.toMutableList()
                if (arg.contains("uuid:")) return Bukkit.getOnlinePlayers().map {"uuid:${it.uniqueId}"}.toMutableList()
            }

            return mutableListOf()
        }

        val size = args?.size ?: 0

        if (executor == VisitCommand) {
            if (size == 1) {
                return worldSpecifier(args!![0])
            }
        }

        if (executor == InviteCommand) {
            if (size == 1) {
                return Bukkit.getOnlinePlayers().map { it.displayName }.toMutableList()
            }
        }

        if (executor == WorldPointCommand) {
            return when (size) {
                1 -> Bukkit.getOnlinePlayers().map { it.displayName }.toMutableList()
                2 -> mutableListOf("add", "subtract", "set", "get")
                else -> mutableListOf()
            }
        }

        if (executor == MWMCommand) {
            if (size == 1) {
                return Option.entries.map {it.toString().lowercase()}.toMutableList()
            }

            val option = Option.valueOf(args!![0].uppercase())

            return when (option) {
                Option.CREATE -> {
                    when(size) {
                        2 -> playerSpecifier(args[1])
                        3 -> MyWorldManager.registeredTemplateWorld.map {it.worldId}.toMutableList()
                        4 -> mutableListOf()
                        else -> mutableListOf()
                    }
                }

                Option.INFO -> {
                    if (size == 2) playerSpecifier(args[1]) else mutableListOf()
                }

                Option.DEACTIVATE -> {
                    if (size == 2) worldSpecifier(args[1]) else mutableListOf()
                }

                Option.ACTIVATE -> {
                    if (size == 2) worldSpecifier(args[1]) else mutableListOf()
                }

                Option.UPDATE -> {
                    if (size == 2) worldSpecifier(args[1]) else mutableListOf()
                }

                Option.START_CREATION_SESSION -> {
                    when(size) {
                        2 -> Bukkit.getOnlinePlayers().map{it.displayName}.toMutableList()
                        else -> mutableListOf()
                    }
                }

                Option.RELOAD -> mutableListOf()
            }
        }

        return mutableListOf()
    }
}
