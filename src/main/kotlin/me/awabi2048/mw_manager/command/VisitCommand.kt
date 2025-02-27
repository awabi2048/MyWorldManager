package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.prefix
import me.awabi2048.mw_manager.my_world.PublishLevel
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object VisitCommand: CommandExecutor, TabCompleter {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        if (p0 !is Player) {
            p0.sendMessage("MVM >> This command is only available on player execution.")
            return true
        }

        if (p3?.size != 1) {
            p0.sendMessage("§c無効なコマンドです。")
            return true
        }

        val targetWorld = Lib.translateWorldSpecifier(p3[0])

        // 引数チェック
        if (targetWorld == null) {
            p0.sendMessage("§c指定されたワールドが見つかりませんでした。")
            return true
        }

        // ワールドの訪問可否チェック
        if (!p0.hasPermission("mw_manager.admin") && targetWorld.publishLevel == PublishLevel.PRIVATE) {
            p0.sendMessage("§7指定されたワールドは§c非公開設定§7であるため、訪問できません。")
            p0.playSound(p0, Sound.ENTITY_SHULKER_HURT, 1.0f, 1.0f)
            return true
        }

        if (p0.hasPermission("mw_manager.admin") && targetWorld.publishLevel == PublishLevel.PRIVATE) {
            p0.sendMessage("$prefix §e管理者権限により、公開設定をバイパスします。")
        }

        // 問題なければワープ
        targetWorld.warpPlayer(p0)
        p0.playSound(p0, Sound.BLOCK_CHEST_OPEN, 1.0f, 2.0f)
        return true
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?,
    ): MutableList<String> {
        return CommandManager.getTabCompletion(p3?.toList(), this)
    }
}
