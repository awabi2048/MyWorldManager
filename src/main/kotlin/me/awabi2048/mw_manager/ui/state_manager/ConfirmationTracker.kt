package me.awabi2048.mw_manager.ui.state_manager

import me.awabi2048.mw_manager.ui.children.ConfirmationUI
import org.bukkit.entity.Player

data class ConfirmationTracker(val player: Player, val uiData: ConfirmationUI.UIData)
