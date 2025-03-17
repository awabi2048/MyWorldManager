package me.awabi2048.mw_manager.command.command_executor

import me.awabi2048.mw_manager.command.CommandManager
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.extension.notify
import me.awabi2048.mw_manager.my_world.MyWorldManager
import org.bukkit.Bukkit
import org.bukkit.Sound
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

        // ワールド引数なし: 現在いるワールドに招待
        if (p3.size == 1) {
            val worldUUID = p0.world.name.substringAfter("my_world.")
            val world = MyWorld(worldUUID)

            // 自分のワールドだけ
            if (world.members?.contains(p0) == false) {
                p0.sendMessage("§c自分以外のワールドには招待できません。")
                p0.playSound(p0, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f)
                return true
            }

            //
            targetWorld = world
            targetPlayer = Bukkit.getPlayer(p3[0])
        } else if (p3.size == 2) {

            targetPlayer = Bukkit.getPlayer(p3[0])
            targetWorld = MyWorldManager.registeredMyWorlds
                .filter {it.members?.contains(p0) == true}
                .find {it.name == p3[1]}

        } else {
            p0.sendMessage("§c無効なコマンドです。 /invite <プレイヤー> [ワールド]")
            return true
        }

        if (targetPlayer == null) {
            p0.sendMessage("§c指定されたプレイヤーが見つかりませんでした。")
            return true
        }

        if (targetWorld == null) {
            p0.sendMessage("§c指定されたプレイヤーが見つかりませんでした。")
            return true
        }

        // 招待送信
        p0.sendMessage("§b${targetPlayer.name}さん §7をあなたのワールドに招待しました！")
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
