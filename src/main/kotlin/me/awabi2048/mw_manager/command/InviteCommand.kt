package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.player_extension.notify
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object InviteCommand: CommandExecutor, TabCompleter {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        if (p0 !is Player) {
            p0.notify("This command is only available on player execution.", null)
            return true
        }

        if (p3.isNullOrEmpty()) {
            p0.notify("§c引数を入力してください。", null)
            return true
        }

        if (p3.size !in 1..2) {
            p0.notify("§c無効な引数です。", null)
            return true
        }

        val player = Bukkit.getPlayer(p3[0])
        val world = Lib.translateWorldSpecifier(p3[1]?: "")

        if (player == null) {
            p0.notify("§c指定されたプレイヤーが見つかりませんでした。", null)
            return true
        }

        if (world == null) {
            p0.notify("§c指定されたワールドが見つかりませんでした。", null)
            return true
        }

        // 招待送信
        world.invitePlayer(p0, player)

        return true
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?,
    ): MutableList<String> {
        when (p3?.size) {
            1 -> return Bukkit.getOnlinePlayers().map { it.displayName }.toMutableList()
            else -> return mutableListOf()
        }
    }
}
