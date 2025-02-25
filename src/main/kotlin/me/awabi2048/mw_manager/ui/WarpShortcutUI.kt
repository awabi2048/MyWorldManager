package me.awabi2048.mw_manager.ui

import me.awabi2048.mw_manager.config.Config
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.player_data.PlayerData
import me.awabi2048.mw_manager.player_notification.PlayerNotification
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class WarpShortcutUI(private val owner: Player) : AbstractInteractiveUI(owner) {
    override fun onClick(event: InventoryClickEvent) {
        event.isCancelled = true
        if (event.slot !in 10..16 && event.slot !in 19..25) return

        val itemName = event.currentItem!!.itemMeta!!.itemName
        if (itemName == "§cロック中") return

        owner.playSound(owner, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)

        if (event.isLeftClick && !event.isShiftClick) {// 新規登録
            if (itemName == "§b未登録") {
                val world = MyWorldManager.registeredMyWorld.find{it.vanillaWorld == owner.world}
                if (world == null) {
                    PlayerNotification.WARP_SHORTCUT_SET_FAILED.send(owner)
                    owner.closeInventory()
                    return
                }

                val uuid = world.uuid
                val playerData = PlayerData(owner)
                playerData.warpShortcuts += uuid

                owner.playSound(owner, Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f)
                owner.closeInventory()

            } else { // 登録済み: ワープ

                val uuid = event.currentItem!!.itemMeta!!.lore!!.find { it.contains("UUID:") }!!.substringAfter("UUID:")
                val targetWorld = MyWorld(uuid)

                targetWorld.warpPlayer(owner)
            }
            // 右クリック → 削除
        } else if (event.isRightClick && !event.isShiftClick) {
            if (itemName != "§b未登録") {

                val uuid = event.currentItem!!.itemMeta!!.lore!!.find { it.contains("UUID:") }!!.substringAfter("UUID:")
                val playerData = PlayerData(owner)
                playerData.warpShortcuts -= uuid

                owner.closeInventory()
                owner.playSound(owner, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f)

            }
        }
    }

    override fun open() {
        //
        owner.openInventory(ui)
        owner.playSound(owner, Sound.ENTITY_ENDERMAN_AMBIENT, 1.0f, 1.0f)
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

        val menu = createTemplate(4, "§8§lWarp Shortcut")!!

        val playerData = PlayerData(owner)

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
            setItemName("§b未設定")
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
}
