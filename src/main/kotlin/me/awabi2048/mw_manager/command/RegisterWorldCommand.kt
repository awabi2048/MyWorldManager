package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.prefix
import me.awabi2048.mw_manager.MyWorld
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

// /register_world uuid:%uuid% %world_name% %limit_day% (for admins or console execution)
// /register_world player:%player_name% %world_name% %limit_day% (for admins or console execution)

object RegisterWorldCommand : CommandExecutor, TabCompleter {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        // check permission
        if (p0 is Player && !p0.hasPermission("pw_manager.register_world")) {
            val message = "$prefix §c権限がありません。"

            p0.sendMessage(message)
            return true
        }

        // check if valid arguments are given
        if (p3?.size != 3) {
            val message = when (p0 is Player) {
                true -> "$prefix §c引数が足りないか、多すぎます。"
                else -> "PWManager >> Invalid arguments size."
            }

            p0.sendMessage(message)
            return true
        }

        val worldName = p3[1]
        val limitDay = p3[2].toIntOrNull()

        // check 1st argument
        val ownerPlayer = Lib.getPlayerFromSpecifier(p3[0])
        if (ownerPlayer == null) {
            val message = when (p0 is Player) {
                true -> "$prefix §c第一引数が無効です。"
                else -> "PWManager >> Invalid second argument."
            }

            p0.sendMessage(message)
            return true
        }

        // check 3rd argument
        if (limitDay == null || limitDay < 0) {
            val message = when (p0 is Player) {
                true -> "$prefix §c第三引数が無効です。"
                else -> "PWManager >> Invalid third argument."
            }

            p0.sendMessage(message)
            return true
        }

        // registration process
        val myWorld = MyWorld(worldName)
        myWorld.register(limitDay)
        return true
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?
    ): MutableList<String>? {
        return when (p3?.size) {
            1 -> mutableListOf("uuid:", "player:")
            2 -> null
            3 -> null
            else -> null
        }
    }
}
