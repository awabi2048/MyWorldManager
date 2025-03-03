package me.awabi2048.mw_manager.ui

import me.awabi2048.mw_manager.data_file.Config
import me.awabi2048.mw_manager.my_world.MyWorld
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
            throw IllegalStateException("Unregistered world given.")
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
            if (player in world.moderators) {
                player.closeInventory()
                player.sendMessage(Component.text("§7招待するプレイヤーの名前を入力してください！"))
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f)
            }
        }

        // メンバーの管理
        if (event.slot in 10..16) {
            if (event.click.isRightClick && event.click.isShiftClick) {
                if (player != world.owner) return

                val targetPlayer = (event.currentItem?.itemMeta as SkullMeta?)?.owningPlayer ?: return

                val confirmationUI =
                    ConfirmationUI(player, ConfirmationUI.UIData.WorldMemberRemove(targetPlayer, world))
                confirmationUI.open(true)
            } else return
        }
    }

    override fun preOpenProcess(firstOpen: Boolean) {
        player.playSound(player, Sound.ENTITY_VILLAGER_TRADE, 1.0f, 1.2f)
    }

    override fun construct(): Inventory {
        val ui = createTemplate(3, "§8§lワールドメンバー")!!

        // 管理者・オーナー以外は招待できない
        if (player in world.moderators) {
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
        world.players!!.forEach { member ->
            val icon = ItemStack(Material.PLAYER_HEAD)
            icon.editMeta {
                (it as SkullMeta).owningPlayer = member

                val onlineStatusPrefix = when (member.isOnline) {
                    true -> "§a§l"
                    false -> "§c"
                }

                val playerPermissionName = when (member) {
                    world.owner!! -> "§cオーナー"
                    in world.moderators -> "§6管理者"
                    else -> "§bメンバー"
                }

                it.itemName(Component.text("${onlineStatusPrefix}${member.name}"))
                when (player == world.owner) {
                    true -> it.lore(
                        listOf(
                            Component.text(bar),
                            Component.text("§7UUID ${member.uniqueId}"),
                            Component.text(bar),
                            Component.text("$index §7権限 $playerPermissionName"),
                            Component.text(bar),
                            Component.text("$index §eShift + 右クリック §7このプレイヤーを追放"),
                            Component.text(bar),
                        )
                    )

                    false -> it.lore(
                        listOf(
                            Component.text(bar),
                            Component.text("§7UUID ${member.uniqueId}"),
                            Component.text(bar),
                            Component.text("$index §7権限 $playerPermissionName"),
                            Component.text(bar),
                        )
                    )
                }
            }

            ui.setItem(world.players!!.indexOf(member) + 10, icon)
        }


        return ui
    }

    override fun onClose(reason: InventoryCloseEvent.Reason) {
    }

    override fun onChatInput(text: String) {
        // プレイヤーの招待の送信
        val targetPlayer = Bukkit.getOfflinePlayer(text)

        if (!targetPlayer.isOnline) {
            player.sendMessage("§cプレイヤーがオフラインであるか、存在しません。再度入力してください。")
            player.closeInventory()
            return
        }

        if (targetPlayer in world.players!!) {
            player.sendMessage("§cそのプレイヤーはすでにメンバーです。再度入力してください。")
            player.closeInventory()
            return
        }

        if (text == Config.cancelFlag) {
            player.sendMessage("§c入力をキャンセルしました。")
            update()
            return
        }

        world.recruitPlayer(player, targetPlayer.player!!)
        update()
    }
}
