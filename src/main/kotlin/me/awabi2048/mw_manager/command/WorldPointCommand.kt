package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.Main.Companion.prefix
import me.awabi2048.mw_manager.player_data.PlayerData
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

// /worldpoint %player% add|subtract|set|get %value%?
object WorldPointCommand : CommandExecutor, TabCompleter {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        if (p3?.size !in 2..3) {
            p0.sendMessage("$prefix §c無効なコマンドです。")
            return true
        }

        // 引数チェック
        val player = Bukkit.getOfflinePlayer(p3!![0]).player
        val option = p3[1]

        if (player == null) {
            p0.sendMessage("$prefix §c該当のプレイヤーが見つかりませんでした。")
            return true
        }

        if (option !in listOf("add", "subtract", "set", "get")) {
            p0.sendMessage("$prefix §c操作が無効です。")
            return true
        }

        val data = PlayerData(player)

        // 値の変更を伴う操作
        if (p3.size == 3) {
            val value = p3[2].toIntOrNull()

            if (value == null) {
                p0.sendMessage("$prefix §c入力値が無効です。")
                return true
            }

            if (option !in listOf("add", "subtract", "set")) {
                p0.sendMessage("$prefix §c操作が無効です。")
                return true
            }

            // 通知のため値を保管する
            val modifiedValue = when (option) {
                "add" -> data.worldPoint + value
                "subtract" -> data.worldPoint - value
                "set" -> data.worldPoint
                else -> data.worldPoint
            }

            data.worldPoint = modifiedValue

            // 結果の通知
            p0.sendMessage("$prefix §e${player.displayName}§7のワールドポイントを§a${modifiedValue}§7に変更しました。")

        } else if (p3.size == 2) {
            if (option != "get") {
                p0.sendMessage("$prefix §c操作が無効です。")
                return true
            }

            // 結果の送信
            p0.sendMessage("$prefix §e${player.displayName}§7のワールドポイントは、現在§a${data.worldPoint}です。")

        }

        return true
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?,
    ): MutableList<String> {
        return mutableListOf()
    }
}
