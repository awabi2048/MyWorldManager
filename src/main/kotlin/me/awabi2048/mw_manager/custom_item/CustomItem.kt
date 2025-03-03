package me.awabi2048.mw_manager.custom_item

import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.Main.Companion.PREFIX
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

enum class CustomItem {
    WORLD_PORTAL;

    val itemStack: ItemStack
        get() {
            val item = when (this) {
                WORLD_PORTAL -> {
                    val item = ItemStack(Material.END_PORTAL_FRAME)
                    item.editMeta {
                        it.itemName(Component.text("§dワールドポータル"))
                        it.lore(
                            listOf(
                                Component.text("§e右クリック§7: 現在のワールドにポータルを紐づけます。"),
                                Component.text("§7§n個人用ワールドのみ§7紐づけ可能です。"),
                                Component.text("§bリンク先§7: なし")
                            )
                        )

                        it.persistentDataContainer.set(
                            NamespacedKey(instance, "portal_linked_world"),
                            PersistentDataType.STRING,
                            "none"
                        )

                        it.setMaxStackSize(1)
                    }

                    // return
                    item
                }
            }

            // IDの設定
            item.editMeta {
                it.persistentDataContainer.set(
                    NamespacedKey(instance, "item_id"),
                    PersistentDataType.STRING,
                    "$this"
                )
            }

            return item
        }

    fun give(player: Player) {
        val item = this.itemStack

        player.inventory.addItem(item)

        player.sendMessage(
            Component.text("$PREFIX ")
                .append(item.itemMeta.itemName())
                .append(Component.text("§7を取得しました。"))
        )
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f)
    }
}
