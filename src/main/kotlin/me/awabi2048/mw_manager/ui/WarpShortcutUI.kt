package me.awabi2048.mw_manager.ui

import me.awabi2048.mw_manager.data_file.Config
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.player_data.PlayerData
import net.kyori.adventure.text.TextComponent
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class WarpShortcutUI(private val player: Player) : AbstractInteractiveUI(player) {
    override fun update() {
        val ui = WarpShortcutUI(player)
        ui.open(false)
    }

    override fun onClick(event: InventoryClickEvent) {
        event.isCancelled = true
        if (event.slot !in 10..16 && event.slot !in 19..25) return

        val itemName = (event.currentItem!!.itemMeta!!.itemName() as TextComponent).content()
        if (itemName == "§cロック中") return

        player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)

        if (event.isLeftClick && !event.isShiftClick) {// 新規登録
            if (itemName == "§b未登録") {
                val world = MyWorldManager.registeredMyWorld.find{it.vanillaWorld == player.world}
                if (world == null) {

                    player.sendMessage("§cこのワールドではワープを設定できません。")
                    player.playSound(player, Sound.ENTITY_SHULKER_HURT, 1.0f, 1.0f)

                    player.closeInventory()
                    return
                }

                val uuid = world.uuid
                val playerData = PlayerData(player)

                // 登録済
                if (uuid in playerData.warpShortcuts) {
                    player.sendMessage("§c既にこのワールドを登録しています。")
                    player.playSound(player, Sound.ENTITY_SHULKER_HURT, 1.0f, 1.0f)
                    return
                }

                // 確認メニュー
                val confirmationUI = ConfirmationUI(player, ConfirmationUI.UIData.AddWarpShortcut(world))
                confirmationUI.open(true)

            } else { // 登録済み: ワープ

                val uuid = event.currentItem!!.itemMeta!!.lore!!.find { it.contains("UUID:") }!!.substringAfter("UUID:")
                val targetWorld = MyWorld(uuid)

                player.sendMessage("§7登録済みのショートカット先へワープします...")

                val isInSameWorld = player.world == targetWorld.vanillaWorld

                targetWorld.warpPlayer(player, !isInSameWorld)
            }

            // 右クリック → 削除
        } else if (event.isRightClick && !event.isShiftClick) {
            if (itemName != "§b未登録") {
                // 確認メニュー
                val uuid = event.currentItem!!.itemMeta!!.lore!!.find { it.contains("UUID:") }!!.substringAfter("UUID:")
                val world = MyWorld(uuid)

                val confirmationUI = ConfirmationUI(player, ConfirmationUI.UIData.RemoveWarpShortcut(world))
                confirmationUI.open(true)
            }
        }
    }

    override fun preOpenProcess(firstOpen: Boolean) {
        //
        player.openInventory(ui)
        if (firstOpen) {
            player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1.0f, 2.0f)
            player.playSound(player, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.0f, 1.2f)
        }
    }

    override fun construct(): Inventory {
        fun getOccupiedSlot(uuid: String): ItemStack {
            val world = MyWorld(uuid)

            val name = world.name!!
            val description = world.description!!
            val iconMaterial = world.iconMaterial!!
            val owner = world.owner!!.name

            val slot = ItemStack(iconMaterial)
            slot.itemMeta = slot.itemMeta.apply {
                setItemName("§a$name")
                lore = listOf(
                    bar,
                    "§7« §b登録済 §7»",
                    bar,
                    "§f§l| §7$description",
                    "§f§l| §7オーナー §a$owner",
                    "§e左クリック§7: このワールドにワープ",
                    "§e右クリック§7: スロットの登録を削除",
                    bar,
                    "§0UUID:$uuid",
                    bar,
                )
            }

            return slot
        }

        val menu = createTemplate(4, "§8§lワープショートカット")!!

        val playerData = PlayerData(player)

        //
        val unavailableIcon = ItemStack(Material.BEDROCK)
        unavailableIcon.itemMeta = unavailableIcon.itemMeta.apply {
            setItemName("§cロック中")
            lore = listOf(
                bar,
                "§7このスロットは利用できません。",
                bar,
            )
        }

        val lockedIcon = ItemStack(Material.TINTED_GLASS)
        lockedIcon.itemMeta = lockedIcon.itemMeta.apply {
            setItemName("§cロック中")
            lore = listOf(
                bar,
                "§7« §c未解放 §7»",
                bar,
                "§7このスロットはアンロックするまで利用できません。",
                bar,
            )
        }

        val emptyIcon = ItemStack(Material.GLASS)
        emptyIcon.itemMeta = emptyIcon.itemMeta.apply {
            setItemName("§b未登録")
            lore = listOf(
                bar,
                "§7« §a解放済 §7»",
                bar,
                "§7クリックしてこのスロットに§e現在のワールド§7を登録します。",
                bar,
            )
        }

        fun slotOf(index: Int): Int {
            val row = index / 7
            val column = index % 7
            return 10 + 9 * row + column
        }

        // set
        for (index in 0..13) menu.setItem(slotOf(index), unavailableIcon)
        for (index in 0..<Config.playerWarpSlotMax) menu.setItem(slotOf(index), lockedIcon)
        for (index in 0..<playerData.unlockedWarpSlot) menu.setItem(slotOf(index), emptyIcon)
        if (playerData.warpShortcuts.isNotEmpty()) {
            for (index in playerData.warpShortcuts.indices) {
                menu.setItem(slotOf(index), getOccupiedSlot(playerData.warpShortcuts[index]))
            }
        }



        return menu
    }

    override fun onClose(reason: InventoryCloseEvent.Reason) {
    }
}
