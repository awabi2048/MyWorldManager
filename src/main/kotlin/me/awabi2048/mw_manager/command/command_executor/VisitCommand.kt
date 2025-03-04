package me.awabi2048.mw_manager.command.command_executor

import me.awabi2048.mw_manager.command.CommandManager
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.my_world.world_property.PublishLevel
import me.awabi2048.mw_manager.ui.top_level.WorldListUI
import org.bukkit.Bukkit
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

        if (!CommandManager.hasCorrectPermission(p0, this)) {
            p0.sendMessage("§c権限がありません。")
            return true
        }

        if (p3?.size != 1) {
            p0.sendMessage("§c無効なコマンドです。")
            return true
        }

        //
        val player = Bukkit.getOfflinePlayer(p3[0])
        if (!MyWorldManager.registeredMyWorld.any {player in it.players!!}) {
            p0.sendMessage("§cプレイヤーが見つからないか、無効です。")
            return true
        }

        val worlds = MyWorldManager.registeredMyWorld.filter {player in it.players!! && it.publishLevel != PublishLevel.PRIVATE}

        if (worlds.isEmpty()) {
            p0.sendMessage("§7表示できるワールドがありませんでした。")
            p0.playSound(p0, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
            return true
        }

        val ui = WorldListUI(p0, worlds, "§8§l${player.name} のワールド")
        ui.open(true)

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
