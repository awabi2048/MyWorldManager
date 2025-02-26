package me.awabi2048.mw_manager.ui

import me.awabi2048.mw_manager.data_file.DataFiles
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class TemplateSelectUI(private val owner: Player) : AbstractUI(owner) {
    override fun open(firstOpen: Boolean) {
        val menu = construct()
        owner.openInventory(menu)

        if (firstOpen) owner.playSound(owner, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
    }

    override fun construct(): Inventory {
        val menu = createTemplate(5, "§8§lTemplate Selection")!!

        val templateWorldList = DataFiles.templateSetting.getKeys(false)

        for (world in templateWorldList) {
            val section = DataFiles.templateSetting.getConfigurationSection(world)!!
            val name = section.getString("name")
            val description = section.getString("description")
            val iconMaterial = Material.valueOf(section.getString("icon", "GRASS_BLOCK")!!.uppercase())
            val unlockState = when(owner.hasPermission("mw_manager.template_unlock.$world")) {
                true -> "§a解放済"
                false -> "§c未解放"
            }

            val icon = ItemStack(iconMaterial)
            icon.itemMeta = icon.itemMeta.apply {
                setItemName(name)
                lore = listOf(
                    "§7---------------",
                    "§0$world",
                    "§b$description",
                    unlockState,
                    "§7---------------"
                )
            }

            val slot = templateWorldList.indexOf(world) + 9
            menu.setItem(slot, icon)
        }

        return menu
    }
}
