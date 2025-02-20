package me.awabi2048.mw_manager.ui

import me.awabi2048.mw_manager.config.DataFiles
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class WarpShortcutUI(private val owner: Player): AbstractUI(owner) {


    override fun open() {
        //
        owner.openInventory(ui)
    }

    override fun construct(): Inventory {
        val menu = createTemplate(4, "§8§lWarp Shortcut")!!

        val playerData = DataFiles.playerData.getConfigurationSection("")

        return menu
    }

}
