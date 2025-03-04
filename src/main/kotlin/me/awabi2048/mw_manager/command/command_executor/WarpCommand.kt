package me.awabi2048.mw_manager.command.command_executor

import me.awabi2048.mw_manager.command.CommandManager
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.player_data.PlayerData
import me.awabi2048.mw_manager.ui.top_level.WarpShortcutUI
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object WarpCommand : CommandExecutor, TabCompleter {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        if (p0 !is Player) {
            p0.sendMessage("MVM >> This command is only available on player execution.")
            return true
        }

        if (!CommandManager.hasCorrectPermission(p0, this)) {
            p0.sendMessage("§c権限がありません。")
            return true
        }

        when (p3?.size) {
            // 引数なし → メニュー開く
            0 -> {
                val menu = WarpShortcutUI(p0)
                menu.open(true)
                return true
            }

            // 引数１ → このスロットのワープを呼び出し
            1 -> {
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

                // もういるよ！！
                if (p0.world == targetWorld.vanillaWorld) {
                    p0.sendMessage("§c既にこのワールドにいます。")
                    return true
                }

                targetWorld.warpPlayer(p0, true)
                return true
            }

            else -> {
                p0.sendMessage("§c無効なコマンドです。")
                return true
            }
        }
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?,
    ): MutableList<String> {
        return CommandManager.getTabCompletion(p0, p3?.toList(), this)
    }
}
