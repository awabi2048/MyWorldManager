package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.Main.Companion.mvWorldManager
import me.awabi2048.mw_manager.Main.Companion.prefix
import me.awabi2048.mw_manager.MyWorld
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

// /mwregister %world_name% %expire_in% (for admins or console execution)

object RegisterWorldCommand : CommandExecutor, TabCompleter {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        // check permission
        if (p0 is Player && !p0.hasPermission("mw_manager.register_world")) {
            val message = "$prefix §c権限がありません。"

            p0.sendMessage(message)
            return true
        }

        // check if valid arguments are given
        if (p3?.size != 2) {
            val message = when (p0 is Player) {
                true -> "$prefix §c引数が足りないか、多すぎます。"
                else -> "MWManager >> Invalid arguments size."
            }

            p0.sendMessage(message)
            return true
        }

        val worldName = p3[0]
        val expireIn = p3[1].toIntOrNull()

        // check 1st argument
        if (!mvWorldManager.mvWorlds.any { it.name.substringBefore("[") == worldName }) {
            val message = when (p0 is Player) {
                true -> "$prefix §c指定されたワールドが見つかりませんでした。"
                else -> "MWManager >> Invalid first argument."
            }

            p0.sendMessage(message)
            return true
        }

        // check 2nd argument
        if (expireIn == null || expireIn < 0) {
            val message = when (p0 is Player) {
                true -> "$prefix §c日数指定が無効です。"
                else -> "MWManager >> Invalid third argument."
            }

            p0.sendMessage(message)
            return true
        }

        // registration process
        val myWorld = MyWorld(worldName)

        val registrationSucceeded = myWorld.register(expireIn)
        if (!registrationSucceeded) println("MWManager >> Registration failed. World: $worldName")

        return true
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?
    ): MutableList<String> {
        return when (p3?.size) {
            1 -> mvWorldManager.mvWorlds.map { it.name }.toMutableList()
            2 -> mutableListOf()
            3 -> mutableListOf()
            else -> mutableListOf()
        }
    }
}
