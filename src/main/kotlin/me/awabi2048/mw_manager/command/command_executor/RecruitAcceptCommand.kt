package me.awabi2048.mw_manager.command.command_executor

import me.awabi2048.mw_manager.Main.Companion.recruitmentCodeMap
import me.awabi2048.mw_manager.command.CommandManager
import me.awabi2048.mw_manager.my_world.world_property.MemberRole
import me.awabi2048.mw_manager.my_world.MyWorld
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object RecruitAcceptCommand: CommandExecutor {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        if (!CommandManager.hasCorrectPermission(p0, this)) {
            p0.sendMessage("§c権限がありません。")
            return true
        }

        // 招待受け入れコマンド: 通常実行しないためプレイヤー側の表示は最低限
        if (p0 !is Player) return true
        if (p3?.size != 1) return true

        val recruitmentCode = p3[0]
        val worldUUID = recruitmentCodeMap[recruitmentCode]?: return true

        val world = MyWorld(worldUUID)

        world.registerPlayer(p0, MemberRole.MEMBER)

        p0.playSound(p0, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
        p0.sendMessage("§7あなたは§8【${world.name}§8】§7の§bワールドメンバー§7になりました！ようこそ！")

        // 確定後は待機データを消去
        recruitmentCodeMap.remove(recruitmentCode)

        return true
    }
}
