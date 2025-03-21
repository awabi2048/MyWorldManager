package me.awabi2048.mw_manager.ui.children

import me.awabi2048.mw_manager.Main.Companion.creationDataSet
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.my_world.TemplateWorld
import me.awabi2048.mw_manager.ui.abstract.AbstractInteractiveUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class TemplateSelectUI(private val player: Player) : AbstractInteractiveUI(player) {
    override fun update() {
        val ui = TemplateSelectUI(player)
        ui.open(false)
    }

    override fun onClick(event: InventoryClickEvent) {
        event.isCancelled = true

        val templateId = (event.currentItem?.itemMeta?.lore()?.get(1) as TextComponent?)?.content()?.substringAfter("§8")?: return
        val templateWorld = TemplateWorld(templateId)

        player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)

        // プレビュー開始
        if (event.click.isLeftClick) {
            // TODO: プレビュー時に読み込みが挟まないようにしたい
            templateWorld.preview(player)
            player.closeInventory()

            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f)
        }

        // 選択
        if (event.click.isRightClick) {
            // 作成途中データに設定
            creationDataSet.find {it.player == player}!!.templateId = templateId

            // 確認メニュー開く
            val confirmationUI = ConfirmationUI(player, ConfirmationUI.UIData.OnCreationTemplate(templateId))
            confirmationUI.open(true)
        }
    }

    override fun preOpenProcess(firstOpen: Boolean) {
        val menu = construct()
        player.openInventory(menu)

        if (firstOpen) player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
    }

    override fun construct(): Inventory {
        val menu = createTemplate(5, "§8§lテンプレートを選択")!!

        MyWorldManager.registeredTemplateWorld.map{it.worldId}.forEach { worldId ->

            val templateWorld = TemplateWorld(worldId)

            val name = templateWorld.name?: return@forEach
            val description = templateWorld.description?: return@forEach
            val iconMaterial = templateWorld.icon?: return@forEach
            val unlockState = when(player.hasPermission("mw_manager.template_unlock.${templateWorld.worldId}")) {
                true -> "§a解放済"
                false -> "§c未解放"
            }

            val icon = ItemStack(iconMaterial)

            icon.editMeta {
                it.itemName(Component.text(name))
                it.lore(listOf(
                    Component.text(bar),
                    Component.text("§8$worldId"),
                    Component.text(bar),
                    Component.text("$index $description"),
                    Component.text("$index $unlockState"),
                    Component.text(bar),
                    Component.text("$index §e左クリック §bプレビュー§fを行います！"),
                    Component.text("$index §e右クリック §fテンプレートをこのワールドに決定します！"),
                    Component.text(bar),
                ))
            }

            val slot = MyWorldManager.registeredTemplateWorld.map{it.worldId}.indexOf(worldId) + 9
            menu.setItem(slot, icon)
        }

        return menu
    }

    override fun onClose(reason: InventoryCloseEvent.Reason) {

    }
}
