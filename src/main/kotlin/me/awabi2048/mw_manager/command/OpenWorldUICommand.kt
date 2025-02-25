package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.ui.WorldManagementUI
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object OpenWorldUICommand : CommandExecutor {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        if (p0 !is Player) {
            p0.sendMessage("MVM >> This command is only available on player execution.")
            return true
        }

        if (!p3.isNullOrEmpty()) {
            p0.sendMessage("§c無効なコマンドです。")
            return true
        }

        val world = MyWorldManager.registeredMyWorld.find { it.vanillaWorld == p0.world }
        if (world == null) {
            p0.sendMessage("§cこのワールドでは使用できないコマンドです。")
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
