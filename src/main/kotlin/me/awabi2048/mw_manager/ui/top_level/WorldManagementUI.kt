package me.awabi2048.mw_manager.ui.top_level

import me.awabi2048.mw_manager.EmojiIcon
import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.Main.Companion.worldSettingState
import me.awabi2048.mw_manager.data_file.Config
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.world_property.PublishLevel
import me.awabi2048.mw_manager.player_data.PlayerData
import me.awabi2048.mw_manager.ui.abstract.AbstractInteractiveUI
import me.awabi2048.mw_manager.ui.children.MemberUI
import me.awabi2048.mw_manager.ui.children.WorldExpandUI
import me.awabi2048.mw_manager.ui.state_manager.PlayerWorldSettingState
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent.Reason
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class WorldManagementUI(private val player: Player, private val world: MyWorld) : AbstractInteractiveUI(player) {
    init {
        if (!world.isRegistered) {
            throw IllegalStateException("Unregistered world given.")
        }
    }

    private fun setIcon(material: Material) {
        world.iconMaterial = material

        // アイテム名を翻訳したいのでComponentを使用
        val message = Component.text("§7ワールドのアイコンを")
            .append(Component.translatable(material.translationKey()).color(AQUA))
            .append(Component.text("§7に変更しました。"))

        player.sendMessage(message)
        player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
        player.playSound(player, Sound.BLOCK_ANVIL_LAND, 1.0f, 2.0f)

        worldSettingState.remove(player)
        val ui = WorldManagementUI(player, world)
        ui.open(false)

        return
    }

    fun setByChatInput(content: String) {
        val state = worldSettingState[player] ?: return

        // ワールド名変更
        if (state == PlayerWorldSettingState.CHANGE_NAME) {
            if (!Lib.checkWorldNameAvailable(content, player)) return

            world.name = content
            player.sendMessage("§eワールドの名前が §6${content} §eに変更されました！")
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
            update()
        }

        // 説明欄変更
        if (state == PlayerWorldSettingState.CHANGE_DESCRIPTION) {
            // ブラックリスト判定
            if (Lib.checkIfContainsBlacklisted(content)) {
                player.sendMessage("§c使用できない文字列が含まれています。再度入力してください。")
                return
            }

            world.description = content
            player.sendMessage("§eワールドの説明が §6${content} §eに変更されました！")
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
            update()
        }

        worldSettingState.remove(player)
    }

    fun setSpawnLocation(location: Location) {
        val state = worldSettingState[player]!!

        when (state) {
            PlayerWorldSettingState.CHANGE_GUEST_SPAWN_POS -> {
                player.sendMessage("§7ゲストのワールドスポーン位置が (§b${location.blockX}§7, §b${location.blockY}§7, §b${location.blockZ}§7)に変更されました。")
                world.guestSpawnLocation = location
            }

            PlayerWorldSettingState.CHANGE_MEMBER_SPAWN_POS -> {
                player.sendMessage("§7メンバーのワールドスポーン位置が (§b${location.blockX}§7, §b${location.blockY}§7, §b${location.blockZ}§7)に変更されました。")
                world.memberSpawnLocation = location
            }

            else -> return
        }

        worldSettingState.remove(player)
        update()
    }

    override fun onClose(reason: Reason) {
        if (worldSettingState[player] == PlayerWorldSettingState.CHANGE_ICON) {
            worldSettingState.remove(player)
            player.sendMessage("§cインベントリを閉じたため、設定をキャンセルしました。")
        }
    }

    override fun onClick(event: InventoryClickEvent) {
        fun timeOut() {
            player.sendMessage("§c一定時間が経過したため、設定をキャンセルしました。")
            worldSettingState.remove(player)
        }

        // アイコン設定中 → 設定する
        if (
            worldSettingState[event.whoClicked] == PlayerWorldSettingState.CHANGE_ICON
            && event.currentItem != null
            && !event.clickedInventory!!.any { it != null && (it.itemMeta.itemName() as TextComponent).content() == "§aワールド表示の変更" } // メニュー内のアイテムは掴めないように
            && event.currentItem?.type !in listOf(
                Material.BLACK_STAINED_GLASS_PANE,
                Material.GRAY_STAINED_GLASS_PANE
            ) // 背景アイテムだと悪用できそうなので
        ) setIcon(event.currentItem!!.type)

        event.isCancelled = true
        val slot = event.slot

        //
        val option = when (slot) {
            19 -> "change_display"
            20 -> "change_spawn_pos"
            21 -> "change_icon"
            23 -> "expand"
            24 -> "change_publish_level"
            25 -> "add_member"
            else -> return
        }

        //
        player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)

        // チャット入力を監視
        if (option == "change_display") {
            if (event.click.isLeftClick) {
                worldSettingState[player] = PlayerWorldSettingState.CHANGE_NAME
                player.sendMessage("§7チャット欄に入力して、§bワールド名の変更§7を行います！")
            }
            if (event.click.isRightClick) {
                worldSettingState[player] = PlayerWorldSettingState.CHANGE_DESCRIPTION
                player.sendMessage("§7チャット欄に入力して、§b説明文の変更§7を行います！")
            }

            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f)

            player.closeInventory()
        }

        // スポーン位置変更
        if (option == "change_spawn_pos") {
            if (event.click.isLeftClick) {
                worldSettingState[player] = PlayerWorldSettingState.CHANGE_MEMBER_SPAWN_POS
                player.sendMessage("§7ブロックをクリックして、§bメンバーのスポーン位置の変更§7を行います！")
            }

            if (event.click.isRightClick) {
                worldSettingState[player] = PlayerWorldSettingState.CHANGE_GUEST_SPAWN_POS
                player.sendMessage("§7ブロックをクリックして、§bゲストのスポーン位置の変更§7を行います！")
            }


            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f)

            player.closeInventory()
        }

        // アイコン変更
        if (option == "change_icon") {
            worldSettingState[player] = PlayerWorldSettingState.CHANGE_ICON
            player.sendMessage("§7インベントリ内のアイテムをクリックして、アイコンを設定します。")
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f)
        }

        // 拡張
        if (option == "expand") {
            val canExpand = PlayerData(player).worldPoint >= world.expandCost!!

            if (canExpand) {
                val expandUI = WorldExpandUI(player, world)

                expandUI.open(false)
                player.playSound(player, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 2.0f)

            } else {
                player.sendMessage("§cワールドポイントが不足しています。")
                player.playSound(player, Sound.ENTITY_SHULKER_HURT, 1.0f, 1.0f)
            }
        }

        // 公開レベル変更
        if (option == "change_publish_level") {
            val currentOptionIndex = PublishLevel.entries.indexOf(world.publishLevel!!)
            val changedIndex = if (event.isLeftClick) (currentOptionIndex + 1).coerceIn(PublishLevel.entries.indices)
            else (currentOptionIndex - 1).coerceIn(PublishLevel.entries.indices)
            val changedOption = PublishLevel.entries[changedIndex]

            world.publishLevel = changedOption

            this.world.publishLevel = changedOption

            update()
        }

        // メンバーを招待
        if (option == "add_member") {
            val memberUI = MemberUI(player, world)
            memberUI.open(false)
        }

        // タイムアウト判定　切り替えのみ即座の設定だから除外
        if (option != "change_publish_level") {
            Bukkit.getScheduler().runTaskLater(
                instance,
                Runnable {
                    if (worldSettingState.keys.contains(player)) timeOut()
                },
                120 * 20L
            )
        }
    }

    override fun update() {
        val ui = WorldManagementUI(player, world)
        ui.open(false)
    }

    override fun preOpenProcess(firstOpen: Boolean) {
        //
        if (firstOpen) player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.2f)
    }

    override fun construct(): Inventory {
        val menu = createTemplate(5, "§8§lWorld Management")!!

        val playerData = PlayerData(player)

        if (!world.isRegistered) throw IllegalArgumentException("Unregistered world for management ui specified.")

        // 総合情報
        val infoIcon = world.iconItem!!

        // 設定ゾーン: 名前・説明
        val changeDisplayIcon = ItemStack(Material.NAME_TAG)
        changeDisplayIcon.itemMeta = changeDisplayIcon.itemMeta.apply {
            Component.text("§aワールド表示の変更")
            lore(
                listOf(
                    Component.text(bar),
                    Component.text("$index §e左クリック§7: §aワールドの名前§7を変更します。"),
                    Component.text("$index §e右クリック§7: §aワールドの説明§7を変更します。"),
                    Component.text(bar),
                )
            )
        }

        // 設定ゾーン: アイコン
        val changeIconIcon = ItemStack(Material.ANVIL)
        changeIconIcon.itemMeta = changeDisplayIcon.itemMeta.apply {
            itemName(Component.text("§aワールド表示の変更"))
            lore(
                listOf(
                    Component.text(bar),
                    Component.text("$index §e左クリック§7: §aワールドのアイコン§7を変更します。"),
                    Component.text("§7インベントリ内のアイテムをクリックして、そのアイテムをアイコンに設定します。"),
                    Component.text(bar),
                )
            )
        }

        // 設定ゾーン: スポーン
        val changeSpawnIcon = ItemStack(Material.ENDER_PEARL)
        changeSpawnIcon.itemMeta = changeSpawnIcon.itemMeta.apply {
            itemName(Component.text("§aスポーン位置の変更"))
            lore(
                listOf(
                    Component.text(bar),
                    Component.text("$index §e左クリック§7: §aメンバーのスポーン位置§7を変更します。"),
                    Component.text("$index §e右クリック§7: §aゲストのスポーン位置§7を変更します。"),
                    Component.text(bar),
                )
            )
        }

        // 機能ゾーン: 拡張
        val expandIcon = ItemStack(Material.CHEST)
        expandIcon.itemMeta = expandIcon.itemMeta.apply {
            // 拡張可能かで中行の表示を変える
            val maxExpanded = world.borderExpansionLevel == Config.borderExpansionMax
            val hasEnoughPoint = playerData.worldPoint >= world.expandCost!!
            val costDisplay = when (hasEnoughPoint) {
                true -> "$index §7コスト ${EmojiIcon.WORLD_POINT} §e${world.expandCost}" +
                        " §7(" +
                        "${world.borderExpansionLevel}" +
                        " §f➡ " +
                        "§e§l${
                            world.borderExpansionLevel?.plus(1)
                        }§7)"

                false -> "$index §7コスト ${EmojiIcon.WORLD_POINT} §c§n${world.expandCost}" +
                        "§7 (" +
                        "${world.borderExpansionLevel}" +
                        " §f➡ " +
                        "§e§l${
                            world.borderExpansionLevel?.plus(1)
                        }§7) §cポイントが§n${world.expandCost!! - playerData.worldPoint}§c不足しています。"
            }

            val info = when (maxExpanded) {
                true -> listOf(
                    "$index §7現在の拡張レベル §e§l${world.borderExpansionLevel}§7/${Config.borderExpansionMax} §c§lMAX",
                    "$index §fこれ以上拡張できません。"
                )

                false -> listOf(
                    "$index §7現在の拡張レベル §e§l${world.borderExpansionLevel}§7/${Config.borderExpansionMax}",
                    costDisplay,
                    "$index §7あなたの所有ポイント: ${EmojiIcon.WORLD_POINT} §e${playerData.worldPoint}",
                )
            }

            itemName(Component.text("§bワールドの拡張"))
            lore(
                (listOf(
                    bar,
                    "§fクリックして§bワールドのボーダー§fを§a1段階拡張§fします。",
                    bar
                ) + info + bar).map { Component.text(it) }
            )
        }

        // 機能ゾーン: 公開レベル
        val publishLevelIcon = ItemStack(Material.COMPASS)
        publishLevelIcon.itemMeta = publishLevelIcon.itemMeta.apply {
            val list = PublishLevel.entries
                .map {if (it == world.publishLevel) "§f▶ ${it.japaneseName}" else "§7${it.japaneseName.drop(2)}"}
                .map {Component.text(it)}

            itemName(Component.text("§bワールド公開レベルの変更"))
            lore(
                listOf(
                    Component.text(bar),
                    Component.text("§fクリックして§eワールドの公開レベルを変更§fします。"),
                    Component.text("$index §7現在の設定 §e${world.publishLevel!!.japaneseName}"),
                    Component.text(bar)
                ) + list + Component.text(bar)
            )
        }

        // 機能ゾーン: メンバー
        val memberIcon = ItemStack(Material.PLAYER_HEAD)
        memberIcon.itemMeta = memberIcon.itemMeta.apply {

            val memberList = world.members!!.map {
                val onlineState = when (it.key.isOnline) {
                    true -> "§a§l"
                    false -> "§c"
                }

                "§8［${it.value.japaneseName}§8］ ${onlineState}${it.key.name}"
            }

            itemName(Component.text("§bメンバー"))
            lore(
                (listOf(
                    bar,
                    "§fクリックして§6メンバーの管理§fを行います。",
                    bar,
                ) + memberList + listOf(
                    bar
                )).map { Component.text(it) })
        }

        // set
        menu.setItem(19, changeDisplayIcon)
        menu.setItem(20, changeSpawnIcon)
        menu.setItem(21, changeIconIcon)
        menu.setItem(23, expandIcon)
        menu.setItem(24, publishLevelIcon)
        menu.setItem(25, memberIcon)
        menu.setItem(40, infoIcon)

        return menu
    }
}
