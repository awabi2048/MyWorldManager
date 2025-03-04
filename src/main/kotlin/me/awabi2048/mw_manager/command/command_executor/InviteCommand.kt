package me.awabi2048.mw_manager.command.command_executor

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.command.CommandManager
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.extension.notify
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

        if (!CommandManager.hasCorrectPermission(p0, this)) {
            p0.sendMessage("§c権限がありません。")
            return true
        }

        if (p3.isNullOrEmpty()) {
            p0.notify("§c引数を入力してください。", null)
            return true
        }

        val targetWorld: MyWorld?
        val targetPlayer: CommandSender?

        if (p3.size == 1) {
            val worldUUID = p0.world.name.substringAfter("my_world.")
            val world = MyWorld(worldUUID)

            // 自分のワールドだけ
            if (world.players?.contains(p0) == false) {
                p0.notify("§c自分以外のワールドには招待できません。", null)
                return true
            }

            //
            targetWorld = world
            targetPlayer = Bukkit.getPlayer(p3[0])
        } else if (p3.size == 2) {
            targetPlayer = Bukkit.getPlayer(p3[0])
            targetWorld = Lib.translateWorldSpecifier(p3[1])

        } else {
            p0.notify("§c無効なコマンドです。", null)
            return true
        }

        if (targetPlayer == null) {
            p0.notify("§c指定されたプレイヤーが見つかりませんでした。", null)
            return true
        }

        if (targetWorld == null) {
            p0.notify("§c指定されたワールドが見つかりませんでした。", null)
            return true
        }

        // 招待送信
        p0.sendMessage("§b${targetPlayer.name}さん §7をワールドに招待しました。")
        targetWorld.invitePlayer(p0, targetPlayer)

        return true
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
