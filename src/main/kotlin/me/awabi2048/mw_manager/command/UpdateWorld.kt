package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.prefix
import me.awabi2048.mw_manager.Main.Companion.registeredWorldData
import me.awabi2048.mw_manager.MyWorld
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.*

// /update_world
// /update_world uuid:%uuid% %world_name% (for admins)
// /update_world player:%player% %world_name% (for admins)

object UpdateWorld : CommandExecutor, TabCompleter {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        // update own world (for citizen)
        if (p3.isNullOrEmpty() && p0 is Player) {
            // check sender
            if (p0 !is Player) {
                p0.sendMessage("PWManager >> Only players can execute this command. Try using /update_world <owner>")
                return true
            }

            // check permission
            if (!p0.hasPermission("pw_manager.command.update_world")) {
                p0.sendMessage("$prefix §c権限がありません。")
                return true
            }

            // check if world registered
            if (!registeredWorldData.contains("${p0.uniqueId}-${p0.world.name}")) {
                p0.sendMessage("$prefix §c登録されたワールドが見つかりませんでした。")
                return true
            }

            // update
            val myWorld = MyWorld(personalWorldName)
            myWorld.update()
            println("PWManager >> Updated world with Name: $personalWorldName, UUID: ${p0.uniqueId}, NewLimitDate: ${myWorld.limitDate}")

            return true

        } else { // update specified owner's world
            // check permission
            if (p0 is Player && !p0.hasPermission("pw_manager.admin")) {
                p0.sendMessage("$prefix §c権限がありません。")
                return true
            }

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

            // check 2nd argument
            val personalWorldName = registeredWorldData.getString("${ownerPlayer.uniqueId}${p0.}")

            // check if world registered
            if (personalWorldName == null) {
                p0.sendMessage("$prefix §c登録されたワールドが見つかりませんでした。")
                return true
            }

            // update
            val myWorld = MyWorld(personalWorldName)
            myWorld.update()
            println("PWManager >> Updated world with Name: $personalWorldName, UUID: ${ownerPlayer.uniqueId}, NewLimitDate: ${myWorld.limitDate}")

            return true
        }
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?
    ): MutableList<String>? {
        if (p0.hasPermission("pw_manager.admin")){
            return when (p3?.size) {
                1 -> mutableListOf("uuid:", "player:")
                else -> null
            }
        } else {
            return null
        }
    }
}
