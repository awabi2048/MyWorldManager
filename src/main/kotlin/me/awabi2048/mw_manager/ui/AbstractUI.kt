package me.awabi2048.mw_manager.ui

import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory

abstract class AbstractUI {
    abstract fun open()

    fun createTemplate(row: Int, title: String) {
        val menu = Bukkit.createInventory(null, row * 9, title)
        
    }
}
