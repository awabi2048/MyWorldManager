package me.awabi2048.mw_manager.ui

import me.awabi2048.mw_manager.Lib
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

abstract class AbstractUI(owner: Player) {
    val ui: Inventory by lazy { construct() }

    val index = "§f§l|"
    val bar: String
        get() {
             return "§7" + "━".repeat(30)
        }

    abstract fun open(firstOpen: Boolean)

    abstract fun construct(): Inventory

    fun createTemplate(row: Int, title: String): Inventory? {
        if (row !in 1..6) return null

        val menu = Bukkit.createInventory(null, row * 9, title)
        val black = Lib.getVirtualItem(Material.BLACK_STAINED_GLASS_PANE)
        val gray = Lib.getVirtualItem(Material.GRAY_STAINED_GLASS_PANE)

        for (iRow in 0..<row) {
            for (iColumn in 0..8) {
                val slot = iRow * 9 + iColumn
                if (iRow == 0 || iRow == row - 1) menu.setItem(slot, black) else menu.setItem(slot, gray)
            }
        }

        return menu
    }
}
