package me.awabi2048.mw_manager.macro_executor

import me.awabi2048.mw_manager.my_world.MyWorld
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

sealed class MacroFlag {
    data class OnWorldCreated(val world: MyWorld, val owner: Player) : MacroFlag()
    data class OnWorldRemoved(val world: MyWorld) : MacroFlag()
    data class OnWorldWarp(val world: MyWorld, val player: Player) : MacroFlag()
    data class OnWorldMemberAdded(val world: MyWorld, val addedPlayer: OfflinePlayer) : MacroFlag()
    data class OnWorldMemberRemoved(val world: MyWorld, val removedPlayer: OfflinePlayer) : MacroFlag()

    val key: String
        get() {
            return when (this) {
                is OnWorldCreated -> "on_world_created"
                is OnWorldRemoved -> "on_world_removed"
                is OnWorldWarp -> "on_world_warp"
                is OnWorldMemberAdded -> "on_member_added"
                is OnWorldMemberRemoved -> "on_member_removed"
            }
        }
}
