package me.awabi2048.mw_manager.ui

import me.awabi2048.mw_manager.my_world.MyWorld
import net.kyori.adventure.text.Component
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class WorldInfoListUI(val owner: Player, val worlds: Set<MyWorld>, val page: Int) : AbstractInteractiveUI(owner) {
    override fun onClick(event: InventoryClickEvent) {

    }

    override fun open(firstOpen: Boolean) {
        owner.openInventory(ui)

        if (firstOpen) owner.playSound(owner, Sound.UI_BUTTON_CLICK, 1.0f, 1.8f)
    }

    override fun construct(): Inventory {
        val ui = createTemplate(6, "§8§lワールドデータの管理")!!
        val displayRange = (page - 1) * 36..<page * 36

        // 与えられたリストからアイテムを配置していく
        worlds.filter { worlds.indexOf(it) in displayRange }.forEach { myWorld ->
//            println(myWorld.iconMaterial)

            val icon = ItemStack(myWorld.iconMaterial!!)

            icon.editMeta { meta ->
                meta.itemName(Component.text("§8【§a${myWorld.name}§8】"))
                meta.lore(
                    myWorld.fixedData.map { Component.text(it) } + listOf(
                        Component.text("$index §7ステータス ${myWorld.activityState?.toJapanese()}"),
                        Component.text(bar),
                        Component.text("$index §e左クリック§7 このワールドに§bワープ§7する"),
                        Component.text("$index §e右クリック§7 このワールドのアーカイブ状態を切り替える"),
                        Component.text("$index §eShift + 右クリック§7 このワールドを§c完全に削除§7する"),
                        Component.text(bar),
                    )
                )

                if (myWorld.isOutDated!!) meta.setEnchantmentGlintOverride(true)
            }

            val slot = worlds.indexOf(myWorld) % 36 + 9
            ui.setItem(slot, icon)
        }

        return ui
    }
}
