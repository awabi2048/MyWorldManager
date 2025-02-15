package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.Main.Companion.prefix
import me.awabi2048.mw_manager.Main.Companion.registeredWorldData
import me.awabi2048.mw_manager.MyWorld
import me.awabi2048.mw_manager.MyWorldManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.*

// /mwupdate
// /mwupdate %worldName% (for admins)

object UpdateWorld : CommandExecutor, TabCompleter {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        // update own world (for citizen)
        if (p3.isNullOrEmpty() && p0 is Player) {
            // check permission
            if (!p0.hasPermission("mw_manager.command.update_world")) {
                p0.sendMessage("$prefix §c権限がありません。")
                return true
            }

            // check if world registered
            if (!registeredWorldData.contains(p0.uniqueId.toString())) {
                p0.sendMessage("$prefix §c登録されたワールドが見つかりませんでした。")
                return true
            }

            // update
            val personalWorldName = p0.world.name
            val myWorld = MyWorld(personalWorldName)
            myWorld.update()
            println("MWManager >> Updated world with player command. Name: $personalWorldName, UUID: ${p0.uniqueId}, NewLimitDate: ${myWorld.expireDate}")

            return true

        } else { // update specific world

            // check permission
            if (p0 is Player && !p0.hasPermission("mw_manager.admin")) {
                p0.sendMessage("$prefix §c権限がありません。")
                return true
            }

            // check argument
            val worldName = p3!![0]
            if (!MyWorldManager.registeredWorld.any {it.worldName == worldName}) {
                p0.sendMessage("$prefix §c登録されたワールドが見つかりませんでした。")
                return true
            }

            // update
            val myWorld = MyWorld(worldName)
            myWorld.update()
            println("MWManager >> Updated world with Name: $worldName, UUID: ${myWorld.owner!!.uniqueId}, NewLimitDate: ${myWorld.expireDate}")

            return true
        }
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?
    ): MutableList<String>? {
        if (p0.hasPermission("mw_manager.admin")){
            return when (p3?.size) {
                1 -> MyWorldManager.registeredWorld.map { it.worldName }.toMutableList()
                else -> null
            }
        } else {
            return null
        }
    }
}
