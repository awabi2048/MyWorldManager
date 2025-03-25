package me.awabi2048.mw_manager.ui.children

import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.ui.abstract.AbstractInteractiveUI
import me.awabi2048.mw_manager.ui.abstract.ChatInputInterface
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class VisitorUI(val player: Player, val myWorld: MyWorld): AbstractInteractiveUI(player), ChatInputInterface {

    init {
        if (!myWorld.isRegistered) {
            throw IllegalArgumentException("登録されていないワールドが指定されました。")
        }
    }

    override fun onClick(event: InventoryClickEvent) {
        val clickedMaterial = event.currentItem?.type?: return
        if (!(clickedMaterial == Material.PLAYER_HEAD || clickedMaterial == Material.FEATHER)) return

        // 招待
        if (clickedMaterial == Material.FEATHER) {
            player.closeInventory(InventoryCloseEvent.Reason.PLUGIN)
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
            player.sendMessage("§e招待するプレイヤーの名前を入力してください。")
        }

        // キック
        if (clickedMaterial == Material.PLAYER_HEAD) {

            val target = (event.currentItem?.itemMeta as? SkullMeta)?.owningPlayer?.player?: return

            myWorld.kickPlayer(player, target)
            player.closeInventory(InventoryCloseEvent.Reason.PLAYER)
        }
    }

    override fun preOpenProcess(firstOpen: Boolean) {
        if (firstOpen) {
            player.playSound(player, Sound.ENTITY_VILLAGER_YES, 1.0f, 2.0f)
        }
    }

    override fun construct(): Inventory {

        val visitors = myWorld.vanillaWorld!!.players.filter {it !in myWorld.members!!.keys}

        val ui = createTemplate(visitors.size / 7 + 3, "§8§lワールドを訪れているプレイヤー")

        // それぞれのプレイヤーのアイコン
        visitors.forEach { player ->
            val index = visitors.indexOf(player)
            val slot = (index / 7 + 1) * 9 + index % 7 + 1

            val item = ItemStack(Material.PLAYER_HEAD)
            item.editMeta {
                it.itemName(Component.text("§e${player.name}"))
                it.lore(listOf(
                    Component.text(bar),
                    Component.text("$index §e左クリック §7このプレイヤーをワールドからキックします。"),
                    Component.text(bar),
                ))
                (it as? SkullMeta)?.setOwningPlayer(player)
            }

            ui.setItem(slot, item)
        }

        // 招待アイコン
        val inviteIcon = ItemStack(Material.FEATHER)
        inviteIcon.editMeta {
            it.itemName(Component.text("§bプレイヤーを招待"))
            it.lore(listOf(
                Component.text("§7クリックしてほかのプレイヤーをワールドに招待します。")
            ))
        }

        ui.setItem(ui.size - 5, inviteIcon)

        return ui
    }

    override fun onClose(reason: InventoryCloseEvent.Reason) {
    }

    override fun update() {
        val ui = VisitorUI(player, myWorld)
        ui.open(false)
    }

    override fun onChatInput(text: String) {
        val targetPlayer = Bukkit.getOnlinePlayers().find {it.name == text}
        if (targetPlayer == null) {
            player.sendMessage("§cプレイヤーが見つかりませんでした。")
            player.closeInventory(InventoryCloseEvent.Reason.PLAYER)

            return
        }

        myWorld.invitePlayer(player, targetPlayer)
    }
}
