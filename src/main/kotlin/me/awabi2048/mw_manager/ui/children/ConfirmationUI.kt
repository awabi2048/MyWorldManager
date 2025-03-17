package me.awabi2048.mw_manager.ui.children

import me.awabi2048.mw_manager.Main.Companion.confirmationTracker
import me.awabi2048.mw_manager.Main.Companion.creationDataSet
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.Main.Companion.PREFIX
import me.awabi2048.mw_manager.my_world.world_create.CreationStage
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.my_world.world_property.MemberRole
import me.awabi2048.mw_manager.my_world.world_property.WorldActivityState
import me.awabi2048.mw_manager.player_data.PlayerData
import me.awabi2048.mw_manager.ui.abstract.AbstractInteractiveUI
import me.awabi2048.mw_manager.ui.state_manager.ConfirmationTracker
import me.awabi2048.mw_manager.ui.top_level.WarpShortcutUI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class ConfirmationUI(val player: Player, private val uiData: UIData) : AbstractInteractiveUI(player) {
    sealed class UIData {
        data class OnCreationName(val worldName: String) : UIData()
        data class OnCreationTemplate(val templateId: String) : UIData()
        data class AddWarpShortcut(val targetWorld: MyWorld) : UIData()
        data class RemoveWarpShortcut(val targetWorld: MyWorld) : UIData()
        data class WorldAdminToggleActivity(val world: MyWorld) : UIData()
        data class WorldAdminDelete(val world: MyWorld) : UIData()

        data class WorldMemberInvite(val player: Player, val world: MyWorld) : UIData()
        data class WorldMemberRemove(val player: OfflinePlayer, val world: MyWorld) : UIData()
        data class WorldOwnerTransfer(val player: OfflinePlayer, val world: MyWorld) : UIData()
    }

    override fun update() {
        val ui = ConfirmationUI(player, uiData)
        ui.open(false)
    }

    private fun onConfirm() {
        when (uiData) {
            is UIData.OnCreationName -> {
                creationDataSet.find { it.player == player }!!.creationStage = CreationStage.CLONE_SOURCE

                player.sendMessage("§7ワールドの名前を §a${uiData.worldName} §7に設定しました！")

                // ソース選択メニュー
                val templateSelectUI = TemplateSelectUI(player)
                templateSelectUI.open(true)
            }

            is UIData.OnCreationTemplate -> {
                // データを保存
                creationDataSet.find { it.player == player }!!.creationStage = CreationStage.WAITING_CREATION

                val creationSession = creationDataSet.find { it.player == player }!!
                creationDataSet.removeIf { it.player == player }

                creationSession.register()

                Bukkit.getScheduler().runTaskLater(
                    instance,
                    Runnable {
                        player.closeInventory()
                    },
                    5L
                )
            }

            is UIData.AddWarpShortcut -> {
                // 登録処理
                val playerData = PlayerData(player)
                playerData.warpShortcuts += uiData.targetWorld.uuid

                player.sendMessage("§8【§a${uiData.targetWorld.name}§8】§7をワープショートカットに§a追加§7しました！")
                player.playSound(player, Sound.BLOCK_SMITHING_TABLE_USE, 1.0f, 1.0f)

                player.closeInventory()
            }

            is UIData.RemoveWarpShortcut -> {
                val playerData = PlayerData(player)
                playerData.warpShortcuts -= uiData.targetWorld.uuid

                player.closeInventory()

                player.sendMessage("§8【§a${uiData.targetWorld.name}§8】§7をワープショートカットから§c削除§7しました。")
                player.playSound(player, Sound.BLOCK_ANVIL_DESTROY, 1.0f, 0.8f)
            }

            is UIData.WorldAdminDelete -> {
                player.sendMessage("$PREFIX §b${uiData.world.name} §7を§c削除§7しました。")

                player.closeInventory()

                Bukkit.getScheduler().runTaskLater( // 同 tick で処理すると、インベントリ閉じるまでの時間が出る
                    instance,
                    Runnable {
                        uiData.world.remove()
                    },
                    5L
                )
            }

            is UIData.WorldAdminToggleActivity -> {
                val activity = when (uiData.world.activityState) {
                    WorldActivityState.ARCHIVED -> WorldActivityState.ACTIVE
                    WorldActivityState.ACTIVE -> WorldActivityState.ARCHIVED
                    null -> null
                } ?: return

                player.closeInventory()
                player.sendMessage("$PREFIX §b${uiData.world.name} §7を${activity.japaneseName}§7に変更しました。")

                Bukkit.getScheduler().runTaskLater( // 同 tick で処理すると、インベントリ閉じるまでの時間が出る
                    instance,
                    Runnable {
                        uiData.world.activityState = activity
                    },
                    5L
                )
            }

            is UIData.WorldMemberInvite -> {
                player.sendMessage("§e${uiData.player.name} §7をワールドメンバーに§b招待§7しました。")
                uiData.world.recruitPlayer(player, uiData.player)

                player.closeInventory(InventoryCloseEvent.Reason.PLAYER)
            }

            is UIData.WorldMemberRemove -> {
                player.sendMessage("§c${uiData.player.name} をワールドメンバーから追放しました。")
                uiData.world.members?.remove(uiData.player)

                player.playSound(player, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.0f)
                player.playSound(player, Sound.ENTITY_PLAYER_DEATH, 1.0f, 0.5f)

                player.closeInventory()
            }

            is UIData.WorldOwnerTransfer -> {
                player.sendMessage("§d${uiData.player.name} にオーナー権限を移譲しました。")

                println(uiData.world.members)
                uiData.world.members = uiData.world.members!!.plus(Pair(uiData.player, MemberRole.OWNER)).toMutableMap()
                uiData.world.members = uiData.world.members!!.plus(Pair(uiData.world.owner!!, MemberRole.MODERATOR)).toMutableMap()

                player.playSound(player, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.0f)

                player.closeInventory()
            }
        }
    }

    private fun onCancel() {
        when (uiData) {
            is UIData.OnCreationName -> {
                // 再度入力待ちにする
                creationDataSet.find { it.player == player }!!.worldName = null
                player.closeInventory()

                player.sendMessage("$PREFIX §7設定を取り消しました。§e新しいワールド名§7を入力してください！")
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f)
            }

            is UIData.OnCreationTemplate -> {
                // テンプレート選択画面を開く
                creationDataSet.find { it.player == player }!!.templateId = null
                val templateSelectUI = TemplateSelectUI(player)
                templateSelectUI.open(false)
            }

            is UIData.AddWarpShortcut -> {
                // ワープメニュー開く
                val warpShortcutUI = WarpShortcutUI(player)
                warpShortcutUI.open(false)
            }

            is UIData.RemoveWarpShortcut -> {
                // ワープメニュー開く
                val warpShortcutUI = WarpShortcutUI(player)
                warpShortcutUI.open(false)
            }

            is UIData.WorldAdminDelete -> {
                player.closeInventory()
            }

            is UIData.WorldAdminToggleActivity -> {
                player.closeInventory()
            }

            is UIData.WorldMemberInvite -> {
                val memberUI = MemberUI(player, uiData.world)
                memberUI.open(false)
            }

            is UIData.WorldMemberRemove -> {
                val memberUI = MemberUI(player, uiData.world)
                memberUI.open(false)
            }

            is UIData.WorldOwnerTransfer -> {
                val memberUI = MemberUI(player, uiData.world)
                memberUI.open(false)
            }
        }
    }

    override fun onClick(event: InventoryClickEvent) {
        event.isCancelled = true
        if (event.slot !in listOf(11, 15)) return
        player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)

        val isConfirmed = event.slot == 11

        // 確定 → 処理進行
        when (isConfirmed) {
            true -> onConfirm()
            false -> onCancel()
        }

        confirmationTracker.removeIf { it.player == player }
    }

    override fun preOpenProcess(firstOpen: Boolean) {
        player.openInventory(ui)
        player.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.0f)

        confirmationTracker.add(ConfirmationTracker(player, uiData))
    }

    override fun construct(): Inventory {
        val ui = createTemplate(3, "§8§l確認")!!

        // はい・いいえアイコン（共通）
        val confirmIcon = ItemStack(Material.GREEN_WOOL)
        confirmIcon.editMeta {
            it.itemName(Component.text("§a確定"))
        }

        val cancelIcon = ItemStack(Material.RED_WOOL)
        cancelIcon.editMeta {
            it.itemName(Component.text("§cキャンセル"))
        }

        // 内容の確認アイコン
        val content = when (uiData) {
            is UIData.OnCreationName -> "§bワールド名§7を §a§n${uiData.worldName}§7 に確定する"
            is UIData.OnCreationTemplate -> "§bワールドテンプレート§7を §6§n${MyWorldManager.registeredTemplateWorld.find { it.worldId == uiData.templateId }?.name}§7 に確定する"

            is UIData.AddWarpShortcut -> "§7ワープショートカットに§8【§a${uiData.targetWorld.name}§8】§7を§a追加§7する"
            is UIData.RemoveWarpShortcut -> "§7ワープショートカットから§8【§a${uiData.targetWorld.name}§8】§7を§c削除§7する"

            is UIData.WorldAdminDelete -> "§8【§a${uiData.world.name}§8】§7を§c完全消去§7する"
            is UIData.WorldAdminToggleActivity -> "§8【§a${uiData.world.name}§8】§7のアーカイブ状態を切り替える"

            is UIData.WorldMemberInvite -> "§e${uiData.player.name}§7を§bワールドメンバーとして招待§7する"
            is UIData.WorldMemberRemove -> "§c${uiData.player.name}をワールドメンバーから追放する"
            is UIData.WorldOwnerTransfer -> "§c${uiData.player.name}にオーナー権限を移譲する"
        }

        val contentInfo = when (uiData) {
            is UIData.OnCreationName -> "§cワールド名はあとから変更できます"
            is UIData.OnCreationTemplate -> "§c§nワールド生成後は変更できません。確定後、ワールドが作成されます！"

            is UIData.AddWarpShortcut -> "§c追加したワールドはいつでも削除できます"
            is UIData.RemoveWarpShortcut -> "§c§nこの操作は復元できません"
            is UIData.WorldAdminDelete -> "§c§l§nこの操作は復元できません"
            is UIData.WorldAdminToggleActivity -> "§cワールドデータが移動されます。"
            is UIData.WorldMemberInvite -> ""
            is UIData.WorldMemberRemove -> ""
            is UIData.WorldOwnerTransfer -> "§c権限の移管後、あなたの権限は§6モデレーター§cに変更されます。"
        }

        val contentIcon = ItemStack(Material.REDSTONE_TORCH)
        contentIcon.editMeta {
            it.itemName(Component.text("§eこの内容でよろしいですか？"))
            it.lore(
                listOf(
                    Component.text(bar),
                    Component.text("$index $content"),
                    Component.text("$index $contentInfo"),
                    Component.text(bar),
                )
            )
        }

        ui.setItem(11, confirmIcon)
        ui.setItem(15, cancelIcon)
        ui.setItem(22, contentIcon)

        return ui
    }

    override fun onClose(reason: InventoryCloseEvent.Reason) {
    }
}
