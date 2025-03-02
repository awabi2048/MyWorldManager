package me.awabi2048.mw_manager.ui

import me.awabi2048.mw_manager.EmojiIcon
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.data_file.Config
import me.awabi2048.mw_manager.my_world.ExpandMethod
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.player_data.PlayerData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class WorldExpandUI(val player: Player, val world: MyWorld) : AbstractInteractiveUI(player) {
    init {
        if (!world.isRegistered) {
            throw IllegalStateException("Unregistered world given.")
        }
    }

    override fun update() {
        val ui = WorldExpandUI(player, world)
        ui.open(false)
    }

    override fun onClick(event: InventoryClickEvent) {
        if (event.slot !in listOf(10, 12, 14, 16)) return

        val expandMethod = when (event.slot) {
            10 -> ExpandMethod.LEFT_UP
            12 -> ExpandMethod.LEFT_DOWN
            14 -> ExpandMethod.RIGHT_DOWN
            16 -> ExpandMethod.RIGHT_UP
            else -> return
        }

        // ポイント処理
        val playerData = PlayerData(player)

        player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
        player.playSound(player, Sound.BLOCK_ANVIL_USE, 1.0f, 0.5f)
        player.closeInventory()

        player.sendMessage("§7おあげちゃんがワールドを拡げてくれています... §6「${Config.oageGanbaruMessage.random()}」")

        // 拡張を実行
        Bukkit.getScheduler().runTaskLater(
            instance,
            Runnable {
                world.expand(expandMethod)
                playerData.worldPoint -= world.expandCost!!

                player.sendMessage("§dワールドが拡張されました！§8【§7${world.borderExpansionLevel!! - 1}§8】§f▶§8【§e§l${world.borderExpansionLevel}§8】 §7(残りポイント ${EmojiIcon.WORLD_POINT} §e${playerData.worldPoint}§7)")
            },
            40L
        )
    }

    override fun preOpenProcess(firstOpen: Boolean) {
    }

    override fun construct(): Inventory {
        val ui = createTemplate(3, "§8§lワールドの拡張")!!

        // 中央


        // 左上
        val methodLeftUp = ItemStack(Material.CHEST)
        methodLeftUp.editMeta {
            it.itemName(Component.text("左上に拡張").color(AQUA))
            it.lore(
                mutableListOf(
                    Component.text(bar),
                    Component.text("§7ワールドを左上(北西)方向に拡張します。"),
                    Component.text(bar),
                    Component.text("§7██"),
                    Component.text("§7█§b█"),
                    Component.text("$index §7灰色: 拡張されるエリア"),
                    Component.text("$index §7水色: 現在のエリア"),
                    Component.text(bar),
                )
            )
        }

        // 左下
        val methodLeftDown = ItemStack(Material.CHEST)
        methodLeftDown.editMeta {
            it.itemName(Component.text("左上に拡張").color(AQUA))
            it.lore(
                mutableListOf(
                    Component.text(bar),
                    Component.text("§7ワールドを左下(南西)方向に拡張します。"),
                    Component.text(bar),
                    Component.text("§7█§b█"),
                    Component.text("§7██"),
                    Component.text("$index §7灰色: 拡張されるエリア"),
                    Component.text("$index §7水色: 現在のエリア"),
                    Component.text(bar),
                )
            )
        }

        // 右下
        val methodRightDown = ItemStack(Material.CHEST)
        methodRightDown.editMeta {
            it.itemName(Component.text("左上に拡張").color(AQUA))
            it.lore(
                mutableListOf(
                    Component.text(bar),
                    Component.text("§7ワールドを右下(南東)方向に拡張します。"),
                    Component.text(bar),
                    Component.text("§b█§7█"),
                    Component.text("§7██"),
                    Component.text("$index §7灰色: 拡張されるエリア"),
                    Component.text("$index §7水色: 現在のエリア"),
                    Component.text(bar),
                )
            )
        }

        // 右上
        val methodRightUp = ItemStack(Material.CHEST)
        methodRightUp.editMeta {
            it.itemName(Component.text("右上に拡張").color(AQUA))
            it.lore(
                mutableListOf(
                    Component.text(bar),
                    Component.text("§7ワールドを右上(北東)方向に拡張します。"),
                    Component.text(bar),
                    Component.text("§7██"),
                    Component.text("§b█§7█"),
                    Component.text("$index §7灰色: 拡張されるエリア"),
                    Component.text("$index §7水色: 現在のエリア"),
                    Component.text(bar),
                )
            )
        }

        // set
        ui.setItem(10, methodLeftUp)
        ui.setItem(12, methodLeftDown)
        ui.setItem(14, methodRightDown)
        ui.setItem(16, methodRightUp)

        return ui
    }

    override fun onClose(reason: InventoryCloseEvent.Reason) {
    }
}
