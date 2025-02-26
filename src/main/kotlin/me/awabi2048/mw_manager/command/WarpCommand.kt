package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.player_data.PlayerData
import me.awabi2048.mw_manager.ui.WarpShortcutUI
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object WarpCommand: CommandExecutor {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        if (p0 !is Player) {
            p0.sendMessage("MVM >> This command is only available on player execution.")
            return true
        }

        if (p3?.size == 0) {

            val menu = WarpShortcutUI(p0)
            menu.open(true)
            return true

        } else if (p3?.size == 1) {

            val shortcutIndex = p3[0].toIntOrNull()?.minus(1)

            if (shortcutIndex == null) {
                p0.sendMessage("§c無効なコマンドです。")
                return true
            }

            val shortcutList = PlayerData(p0).warpShortcuts
            if (shortcutIndex !in shortcutList.indices) {
                p0.sendMessage("§c無効なスロット指定です。")
                return true
            }

            val targetUUID = shortcutList[shortcutIndex]
            val targetWorld = MyWorld(targetUUID)

            targetWorld.warpPlayer(p0)
            return true

        } else {
            p0.sendMessage("§c無効なコマンドです。")
            return true
        }
    }
}
