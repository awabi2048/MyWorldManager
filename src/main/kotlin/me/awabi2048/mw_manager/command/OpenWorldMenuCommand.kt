package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.ui.WorldManagementUI
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object OpenWorldMenuCommand: CommandExecutor {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        if (p0 !is Player) {
            p0.sendMessage("MVM >> This command is only available on player execution.")
            return true
        }

        val worldUUID: String?

        when(p3?.size) {
            0 -> {
                if (!p0.world.name.startsWith("my_world.")) {
                    p0.sendMessage("§c無効なコマンドです。")
                    return true
                }

                worldUUID = p0.world.name.substringAfter("my_world.")
            }
            1 -> worldUUID = p3[0]
            else -> {
                p0.sendMessage("§c無効なコマンドです。")
                return true
            }
        }

        val world = MyWorld(worldUUID)

        if (!world.isRegistered) {
            p0.sendMessage("§c当該のワールドは見つかりませんでした。。")
            return true
        }

        if (p0 !in world.members!!) {
            p0.sendMessage("§cメニューを開くには、ワールドのメンバーである必要があります。")
            return true
        }

        val menu = WorldManagementUI(p0, world)
        menu.open()

        return true
    }
}
