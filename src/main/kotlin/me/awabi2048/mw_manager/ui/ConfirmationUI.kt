package me.awabi2048.mw_manager.ui

import me.awabi2048.mw_manager.Main.Companion.confirmationTracker
import me.awabi2048.mw_manager.Main.Companion.creationDataSet
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.Main.Companion.prefix
import me.awabi2048.mw_manager.my_world.CreationStage
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.player_data.PlayerData
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class ConfirmationUI(val owner: Player, private val uiData: UIData) : AbstractInteractiveUI(owner) {
    sealed class UIData {
        data class OnCreationName(val worldName: String) : UIData()
        data class OnCreationTemplate(val templateId: String) : UIData()
        data class AddWarpShortcut(val targetWorld: MyWorld) : UIData()
        data class RemoveWarpShortcut(val targetWorld: MyWorld) : UIData()
        data class WorldAdminDelete(val world: MyWorld): UIData()
    }

    private fun onConfirm() {
        when (uiData) {
            is UIData.OnCreationName -> {
                creationDataSet.find { it.player == owner }!!.creationStage = CreationStage.CLONE_SOURCE

                owner.sendMessage("§7ワールドの名前を §a${uiData.worldName} §7に設定しました！")

                // ソース選択メニュー
                val templateSelectUI = TemplateSelectUI(owner)
                templateSelectUI.open(true)
            }

            is UIData.OnCreationTemplate -> {
                // データを保存
                creationDataSet.find { it.player == owner }!!.creationStage = CreationStage.WAITING_CREATION

                val creationSession = creationDataSet.find { it.player == owner }!!
                creationDataSet.removeIf { it.player == owner }

                creationSession.register()

                Bukkit.getScheduler().runTaskLater(
                    instance,
                    Runnable {
                        owner.closeInventory()
                    },
                    5L
                )
            }

            is UIData.AddWarpShortcut -> {
                // 登録処理
                val playerData = PlayerData(owner)
                playerData.warpShortcuts += uiData.targetWorld.uuid

                owner.sendMessage("§8【§a${uiData.targetWorld.name}§8】§7をワープショートカットに§a追加§7しました！")
                owner.playSound(owner, Sound.BLOCK_SMITHING_TABLE_USE, 1.0f, 1.0f)

                owner.closeInventory()
            }

            is UIData.RemoveWarpShortcut -> {
                val playerData = PlayerData(owner)
                playerData.warpShortcuts -= uiData.targetWorld.uuid

                owner.closeInventory()

                owner.sendMessage("§8【§a${uiData.targetWorld.name}§8】§7をワープショートカットから§c削除§7しました。")
                owner.playSound(owner, Sound.BLOCK_ANVIL_DESTROY, 1.0f, 0.8f)
            }

            is UIData.WorldAdminDelete -> {
                owner.sendMessage("$prefix §b${uiData.world.name} §7を§c削除§7しました。")
                uiData.world.delete()
            }
        }
    }

    private fun onCancel() {
        when (uiData) {
            is UIData.OnCreationName -> {
                // 再度入力待ちにする
                creationDataSet.find { it.player == owner }!!.worldName = null
                owner.closeInventory()

                owner.sendMessage("$prefix §7設定を取り消しました。§e新しいワールド名§7を入力してください！")
                owner.playSound(owner, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f)
            }

            is UIData.OnCreationTemplate -> {
                // テンプレート選択画面を開く
                creationDataSet.find { it.player == owner }!!.templateId = null
                val templateSelectUI = TemplateSelectUI(owner)
                templateSelectUI.open(false)
            }

            is UIData.AddWarpShortcut -> {
                // ワープメニュー開く
                val warpShortcutUI = WarpShortcutUI(owner)
                warpShortcutUI.open(false)
            }

            is UIData.RemoveWarpShortcut -> {
                // ワープメニュー開く
                val warpShortcutUI = WarpShortcutUI(owner)
                warpShortcutUI.open(false)
            }

            is UIData.WorldAdminDelete -> {
                owner.closeInventory()
            }
        }
    }

    override fun onClick(event: InventoryClickEvent) {
        event.isCancelled = true
        if (event.slot !in listOf(11, 15)) return
        owner.playSound(owner, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)

        val isConfirmed = event.slot == 11

        // 確定 → 処理進行
        when (isConfirmed) {
            true -> onConfirm()
            false -> onCancel()
        }

        confirmationTracker.removeIf{it.player == owner}
    }

    override fun open(firstOpen: Boolean) {
        owner.openInventory(ui)
        owner.playSound(owner, Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.0f)

        confirmationTracker.add(ConfirmationTracker(owner, uiData))
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
        }

        val contentInfo = when (uiData) {
            is UIData.OnCreationName -> "§cワールド名はあとから変更できます"
            is UIData.OnCreationTemplate -> "§c§nワールド生成後は変更できません。確定後、ワールドが作成されます！"

            is UIData.AddWarpShortcut -> "§c追加したワールドはいつでも削除できます"
            is UIData.RemoveWarpShortcut -> "§c§nこの操作は復元できません"
            is UIData.WorldAdminDelete -> "§c§l§nこの操作は復元できません"
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
}
