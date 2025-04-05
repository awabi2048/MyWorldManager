package me.awabi2048.mw_manager.ui.children

import me.awabi2048.mw_manager.data_file.Config
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.world_property.MemberRole
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

class MemberUI(val player: Player, val world: MyWorld) : AbstractInteractiveUI(player), ChatInputInterface {

    init {
        if (!world.isRegistered) {
            throw IllegalStateException("Unregistered world given: ${world.uuid}")
        }
    }

    override fun update() {
        val ui = MemberUI(player, world)
        ui.open(false)
    }

    override fun onClick(event: InventoryClickEvent) {
        if (!(event.slot in 10..16 || event.slot == 22)) return

        // プレイヤー招待
        if (event.slot == 22) {
            if (world.members!![player] != MemberRole.MEMBER) {
                player.closeInventory()
                player.sendMessage(Component.text("§7招待するプレイヤーの名前を入力してください！"))
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f)
            }
        }

        // メンバーの管理
        if (event.slot in 10..16) {

            // 左 → 権限切り替え
            if (event.click.isLeftClick) {
                // オーナー以外は変更不可
                if (world.members!![player] != MemberRole.OWNER) return

                val targetPlayer = (event.currentItem?.itemMeta as SkullMeta?)?.owningPlayer ?: return
                val targetPlayerRole = world.members!![targetPlayer] ?: return

                // ターゲットがオーナーならキャンセル
                if (world.members!![targetPlayer] == MemberRole.OWNER) return

                val changedRole = when (targetPlayerRole) {
                    MemberRole.MEMBER -> MemberRole.MODERATOR
                    MemberRole.MODERATOR -> MemberRole.MEMBER
                    else -> targetPlayerRole
                }

                world.members = world.members!!.plus(Pair(targetPlayer, changedRole)).toMutableMap()

                player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
                player.sendMessage("§e ${targetPlayer.name} §7の権限を ${changedRole.japaneseName} §7に変更しました。")
                update()
            }

            // Shift + 右 → 追放
            if (event.click.isRightClick && event.click.isShiftClick) {
                println((event.currentItem?.itemMeta as? SkullMeta?)?.owningPlayer)

                // オーナー以外は変更不可
                if (world.members!![player] != MemberRole.OWNER) return

                val targetPlayer = (event.currentItem?.itemMeta as? SkullMeta?)?.owningPlayer ?: return

                val confirmationUI =
                    ConfirmationUI(player, ConfirmationUI.UIData.WorldMemberRemove(targetPlayer, world))
                confirmationUI.open(true)
            }

            // 右 → 権限委譲
            if (event.click.isRightClick && !event.click.isShiftClick) {
                // オーナー以外は変更不可
                if (world.members!![player] != MemberRole.OWNER) return

                val targetPlayer = (event.currentItem?.itemMeta as SkullMeta?)?.owningPlayer ?: return

                val confirmationUI =
                    ConfirmationUI(player, ConfirmationUI.UIData.WorldOwnerTransfer(targetPlayer, world))
                confirmationUI.open(true)
            }
        }
    }

    override fun preOpenProcess(firstOpen: Boolean) {
        if (firstOpen) {
            player.playSound(player, Sound.ENTITY_VILLAGER_TRADE, 1.0f, 1.2f)
        }
    }

    override fun construct(): Inventory {
        val ui = createTemplate(3, "§8§lワールドメンバー")!!

        // モデレーター・オーナー以外は招待できない
        if (world.members!![player] != MemberRole.MEMBER) {
            val inviteIcon = ItemStack(Material.FILLED_MAP)
            inviteIcon.editMeta {
                it.itemName(Component.text("§bメンバーに招待する"))
                it.lore(
                    listOf(
                        Component.text(bar),
                        Component.text("$index §7ほかのプレイヤーをワールドメンバーに招待します。"),
                        Component.text("$index §7新しく参加するプレイヤーは§bメンバー§7の状態になります。"),
                        Component.text(bar),
                    )
                )
            }

            ui.setItem(22, inviteIcon)
        }

        // メンバーアイコン
        world.members!!.map { it.key }.forEach { member ->
            val icon = ItemStack(Material.PLAYER_HEAD)
            icon.editMeta {
                (it as SkullMeta).setOwningPlayer(member)

                val onlineStatusPrefix = when (member.isOnline) {
                    true -> "§a§l"
                    false -> "§c"
                }

                val memberRole = world.members!![member]

                val roleChangingTo = when (memberRole) {
                    MemberRole.MODERATOR -> MemberRole.MEMBER
                    MemberRole.MEMBER -> MemberRole.MODERATOR
                    else -> null
                }

                it.itemName(Component.text("${onlineStatusPrefix}${member.name}"))

                // 権限によって表示変更
                if (world.members!![player] == MemberRole.OWNER && world.members!![member] != MemberRole.OWNER) it.lore(
                    listOf(
                        Component.text(bar),
                        Component.text("§7UUID ${member.uniqueId}"),
                        Component.text(bar),
                        Component.text("$index §7権限 ${memberRole?.japaneseName}"),
                        Component.text(bar),
                        Component.text("$index §e左クリック §7このプレイヤーの権限を${roleChangingTo?.japaneseName}§7にする"),
                        Component.text("$index §e右クリック §7このプレイヤーに§cオーナー権限を移譲§7する"),
                        Component.text("$index §eShift + 右クリック §7このプレイヤーを§n追放"),
                        Component.text(bar),
                    )
                ) else it.lore(
                    listOf(
                        Component.text(bar),
                        Component.text("§7UUID ${member.uniqueId}"),
                        Component.text(bar),
                        Component.text("$index §7権限 ${memberRole?.japaneseName}"),
                        Component.text(bar),
                    )
                )
            }

            ui.setItem(world.members!!.map { it.key }.indexOf(member) + 10, icon)
        }

        return ui
    }

    override fun onClose(reason: InventoryCloseEvent.Reason) {
    }

    override fun onChatInput(text: String) {
        // プレイヤーの招待の送信
        val targetPlayer = Bukkit.getOfflinePlayer(text)

        if (text == Config.cancelFlag) {
            player.sendMessage("§c入力をキャンセルしました。")
            update()
            return
        }

        if (!targetPlayer.isOnline) {
            player.sendMessage("§cプレイヤーがオフラインであるか、存在しません。再度入力してください。(${Config.cancelFlag}でキャンセル)")
            player.closeInventory()
            return
        }

        if (targetPlayer in world.members!!) {
            player.sendMessage("§cそのプレイヤーはすでにメンバーです。再度入力してください。(${Config.cancelFlag}でキャンセル)")
            player.closeInventory()
            return
        }

        world.recruitPlayer(player, targetPlayer.player!!)
        update()

        player.sendMessage("§e${targetPlayer.name} をワールドメンバーに招待しました！")
    }
}
