package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.prefix
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

// /mwcheck %world_name%
// /mwcheck list <-remove>

object CheckWorldUpdated: CommandExecutor, TabCompleter {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        // check permission
        if (p0 is Player && !p0.hasPermission("mw_manager.register_world")) {
            val message = "$prefix §c権限がありません。"

            p0.sendMessage(message)
            return true
        }

        // check argument
        if (p3.isNullOrEmpty()) {
            val message = when (p0 is Player) {
                true -> "$prefix §c無効な引数です。"
                else -> "MWManager >> Invalid argument."
            }

            p0.sendMessage(message)
            return true
        }

        // list display
        if (p3[0] == "list") {

        // player specific display
        } else {
            val ownerPlayer = Lib.getPlayerFromSpecifier(p3[0])
            if (ownerPlayer == null) {
                val message = when (p0 is Player) {
                    true -> "$prefix §c第一引数が無効です。"
                    else -> "MWManager >> Invalid second argument."
                }

                p0.sendMessage(message)
                return true
            }


        }

        return true
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
