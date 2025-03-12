package me.awabi2048.mw_manager.listener

import me.awabi2048.mw_manager.Main.Companion.PREFIX
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import java.time.LocalDate

object MiscListener: Listener {
    @EventHandler
    fun onAdminLogin(event: PlayerLoginEvent) {
        if (event.player.hasPermission("mw_manager.admin")) {
            // 期限切れワールドの情報を通知

            event.player.sendMessage("$PREFIX §7${LocalDate.now()}時点で、§e＿個§7のワールドが期限切れ状態です。")
        }
    }
}
