package me.awabi2048.mw_manager.listener

import me.awabi2048.mw_manager.Main.Companion.playerUIState
import me.awabi2048.mw_manager.Main.Companion.worldSettingState
import me.awabi2048.mw_manager.ui.state_manager.PlayerWorldSettingState
import me.awabi2048.mw_manager.ui.top_level.WorldManagementUI
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

object WorldSettingListener : Listener {
    @EventHandler
    fun onPlayerClickBlock(event: PlayerInteractEvent) {
        if (event.action !in listOf(Action.RIGHT_CLICK_BLOCK, Action.LEFT_CLICK_BLOCK)) return
        if (worldSettingState[event.player] !in listOf(
                PlayerWorldSettingState.CHANGE_MEMBER_SPAWN_POS,
                PlayerWorldSettingState.CHANGE_GUEST_SPAWN_POS
            )
        ) return

        event.isCancelled = true

        val location = event.clickedBlock?.location?.toBlockLocation()?.add(0.0, 1.0, 0.0) ?: return

        val ui = playerUIState[event.player] as WorldManagementUI? ?: return
        ui.setSpawnLocation(location)
    }
}
