package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.command.Option.*
import me.awabi2048.mw_manager.command.command_executor.*
import me.awabi2048.mw_manager.custom_item.CustomItem
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.MyWorldManager
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

object CommandManager {
    fun setExecutor() {
        instance.getCommand("myworldmanager")?.setExecutor(MWMCommand)
        instance.getCommand("mwmanager")?.setExecutor(MWMCommand)
        instance.getCommand("mwm")?.setExecutor(MWMCommand)

        instance.getCommand("invite")?.setExecutor(InviteCommand)
        instance.getCommand("visit")?.setExecutor(VisitCommand)

        instance.getCommand("worldpoint")?.setExecutor(WorldPointCommand)
        instance.getCommand("mwm_invite_accept")?.setExecutor(InviteAcceptCommand)
        instance.getCommand("mwm_recruit_accept")?.setExecutor(RecruitAcceptCommand)

        instance.getCommand("worldmenu")?.setExecutor(WorldMenuCommand)
        instance.getCommand("worldwarp")?.setExecutor(WarpCommand)
        instance.getCommand("myworld")?.setExecutor(MyWorldCommand)

        instance.getCommand("mwm_help")?.setExecutor(HelpCommand)
    }

    fun hasCorrectPermission(sender: CommandSender, executor: CommandExecutor): Boolean {
        if (sender is ConsoleCommandSender) return true

        return if (sender is Player) {
            when (executor) {
                MWMCommand -> sender.hasPermission("mw_manager.command.mwm")
                InviteCommand -> sender.hasPermission("mw_manager.command.invite")
                VisitCommand -> sender.hasPermission("mw_manager.command.visit")
                WorldPointCommand -> sender.hasPermission("mw_manager.command.worldpoint")
                InviteAcceptCommand -> sender.hasPermission("mw_manager.command.mwm_invite_accept")
                RecruitAcceptCommand -> sender.hasPermission("mw_manager.command.mwm_recruit_accept")
                WorldMenuCommand -> sender.hasPermission("mw_manager.command.worldmenu")
                WarpCommand -> sender.hasPermission("mw_manager.command.worldwarp")
                HelpCommand -> sender.hasPermission("mw_manager.command.mwm_help")
                else -> false
            }
        } else false
    }

    // size → 確定したもの + 入力中のもの　(デフォルトで1からスタート)
    fun getTabCompletion(sender: CommandSender, args: List<String>?, executor: CommandExecutor): MutableList<String> {
        fun worldSpecifier(arg: String, availablePlayers: Set<Player>, availableWorlds: Set<MyWorld>): MutableList<String> {
            if (!arg.contains(":")) {
                return availablePlayers.map { "${it.name}:" }.toMutableList()
            } else {
                val playerName = arg.substringBefore(":")
                val playerUUID = Bukkit.getPlayer(playerName)?.uniqueId ?: return mutableListOf()
                return availableWorlds.filter { it.owner!!.uniqueId == playerUUID }
                    .map { "$playerName:${it.name!!}" }.toMutableList()
            }
        }

        fun playerSpecifier(arg: String, availablePlayers: Set<Player>): MutableList<String> {
            if (!arg.contains(":")) {
                return mutableListOf("player:", "uuid:")
            } else {
                if (arg.contains("player:")) return availablePlayers.map { "player:${it.name}" }
                    .toMutableList()
                if (arg.contains("uuid:")) return availablePlayers.map { "uuid:${it.uniqueId}" }
                    .toMutableList()
            }

            return mutableListOf()
        }

        val size = args?.size ?: return mutableListOf()
        val onlinePlayers = Bukkit.getOnlinePlayers().toSet()
        val registeredWorlds = MyWorldManager.registeredMyWorlds.toSet()

        if (executor == VisitCommand) {
            if (sender !is Player) return mutableListOf()

            if (size == 1) {
                return Bukkit.getOnlinePlayers().map { it.name }.toMutableList()
            }
        }

        if (executor == InviteCommand) {
            if (sender !is Player) return mutableListOf()

            return when(size) {
                1 -> Bukkit.getOnlinePlayers().filter { it != sender }.map { it.name }.toMutableList()
                2 -> MyWorldManager.registeredMyWorlds.filter {it.members?.contains(sender) == true}.map {it.name!!}.toMutableList()
                else -> mutableListOf()
            }
        }

        if (executor == MWMCommand) {
            if (size == 1) return Option.entries.map { it.toString().lowercase() }.toMutableList()
            if (args[0] !in Option.entries.map { it.name.lowercase() }) return mutableListOf()
            val option = Option.valueOf(args[0].uppercase())

            return when (option) {
                CREATE -> {
                    when (size) {
                        2 -> playerSpecifier(args[1], onlinePlayers)
                        3 -> MyWorldManager.registeredTemplateWorld.map { it.worldId }.toMutableList()
                        4 -> mutableListOf()
                        else -> mutableListOf()
                    }
                }

                INFO -> {
                    if (size == 2) playerSpecifier(args[1], onlinePlayers) else mutableListOf()
                }

                ARCHIVE -> {
                    when (size) {
                        2 -> worldSpecifier(args[1], onlinePlayers, registeredWorlds)
                        else -> mutableListOf()
                    }
                }

                ACTIVATE -> {
                    when (size) {
                        2 -> worldSpecifier(args[1], onlinePlayers, registeredWorlds)
                        else -> mutableListOf()
                    }
                }

                UPDATE -> {
                    if (size == 2) worldSpecifier(args[1], onlinePlayers, registeredWorlds) else mutableListOf()
                }

                START_CREATION_SESSION -> {
                    when (size) {
                        2 -> Bukkit.getOnlinePlayers().map { it.name }.toMutableList()
                        else -> mutableListOf()
                    }
                }

                GET_ITEM -> {
                    when (size) {
                        2 -> CustomItem.entries.map { it.name }.toMutableList()
                        else -> mutableListOf()
                    }
                }

                RELOAD -> mutableListOf()

                PLAYER_DATA -> {
                    when (size) {
                        2 -> playerSpecifier(args[1], onlinePlayers)
                        3 -> mutableListOf("add", "set", "subtract", "get")
                        4 -> mutableListOf("unlocked_world_slot", "unlocked_warp_slot", "world_point")
                        else -> mutableListOf()
                    }
                }

//                SETUP_TEMPLATE -> {
//                    when(size) {
//                        2 -> mutableListOf("<テンプレートID>")
//                        3 -> mutableListOf("<ワールド名>")
//                        4 -> mutableListOf("<説明>")
//                        else -> mutableListOf()
//                    }
//                }
            }
        }

        return mutableListOf()
    }
}
