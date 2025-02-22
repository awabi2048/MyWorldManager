package me.awabi2048.mw_manager.custom_item

import me.awabi2048.mw_manager.Main.Companion.instance
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

enum class CustomItem {
    WORLD_PORTAL;

    fun get(customItem: CustomItem): ItemStack {
        return when(customItem) {
            WORLD_PORTAL -> {
                val item = ItemStack(Material.STRIPPED_CHERRY_WOOD)
                item.itemMeta = item.itemMeta.apply {
                    setItemName("§dワールドポータル")
                    lore = listOf(
                        "§e右クリック§7して使用します"
                    )

                    persistentDataContainer.set(
                        NamespacedKey(instance, "item_id"),
                        PersistentDataType.STRING,
                        "$customItem"
                    )

                    persistentDataContainer.set(
                        NamespacedKey(instance, "portal_linked_world"),
                        PersistentDataType.STRING,
                        "none"
                    )
                }

                // return
                item
            }
        }
    }
}
