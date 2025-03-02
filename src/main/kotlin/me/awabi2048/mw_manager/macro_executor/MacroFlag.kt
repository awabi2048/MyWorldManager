package me.awabi2048.mw_manager.macro_executor

import me.awabi2048.mw_manager.my_world.MyWorld
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

sealed class MacroFlag {
    data class OnWorldCreate(val world: MyWorld, val owner: Player): MacroFlag()
    data class OnWorldWarp(val world: MyWorld, val player: Player): MacroFlag()
    data class OnWorldMemberAdded(val world: MyWorld, val addedPlayer: OfflinePlayer): MacroFlag()
    data class OnWorldMemberRemoved(val world: MyWorld, val removedPlayer: OfflinePlayer): MacroFlag()

    val key: String
        get() {
            return when(this) {
                is OnWorldCreate -> "on_world_create"
                is OnWorldWarp -> "on_world_warp"
                is OnWorldMemberAdded -> "on_member_added"
                is OnWorldMemberRemoved -> "on_member_removed"
            }
        }
}
