package me.awabi2048.mw_manager.ui

import me.awabi2048.mw_manager.EmojiIconPrefix
import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.Main.Companion.worldSettingState
import me.awabi2048.mw_manager.config.Config
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.PublishLevel
import me.awabi2048.mw_manager.player_data.PlayerData
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.exp

class WorldManagementUI(private val owner: Player, private val world: MyWorld) : AbstractInteractiveUI(owner) {
    init {
        if (!world.isRegistered) {
            throw IllegalStateException("Unregistered world given.")
        }
    }

    override fun onClick(event: InventoryClickEvent) {
        fun timeOut() {
            owner.sendMessage("§c一定時間が経過したため、設定をキャンセルしました。")
            worldSettingState.remove(owner)
        }

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
        owner.playSound(owner, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)

        // チャット入力を監視
        if (option == "change_display") {
            if (event.isLeftClick) worldSettingState[owner] = PlayerWorldSettingState.CHANGE_NAME
            if (event.isRightClick) worldSettingState[owner] = PlayerWorldSettingState.CHANGE_DESCRIPTION

            owner.closeInventory()
        }

        // メニューを閉じる　→　プレイヤーがクリックした座標を新たに設定
        if (option == "change_spawn_pos") {
            if (event.isLeftClick) worldSettingState[owner] = PlayerWorldSettingState.CHANGE_MEMBER_SPAWN_POS
            if (event.isRightClick) worldSettingState[owner] = PlayerWorldSettingState.CHANGE_GUEST_SPAWN_POS

            owner.closeInventory()
        }

        // メニュー閉じない　インベントリ内でクリックしたアイテムのMaterialをアイコンとして設定
        if (option == "change_icon") {
            worldSettingState[owner] = PlayerWorldSettingState.CHANGE_ICON
        }

        // 専用メニュー開く
        if (option == "expand") {
            val expandUI = WorldExpandUI(owner, world)
            expandUI.open()
        }

        // 順番に切り替え
        if (option == "change_publish_level") {
            val currentOptionIndex = PublishLevel.entries.indexOf(world.publishLevel!!)
            val changedIndex: Int
            if (event.isLeftClick) changedIndex = (currentOptionIndex + 1).coerceIn(PublishLevel.entries.indices)
            else changedIndex = (currentOptionIndex - 1).coerceIn(PublishLevel.entries.indices)
            val changedOption = PublishLevel.entries[changedIndex]

            world.publishLevel = changedOption
            open()
        }

        // チャット入力を監視　入力されたものをMCIDとして招待
        if (option == "add_member") {
            worldSettingState[owner] = PlayerWorldSettingState.ADD_MEMBER

            owner.closeInventory()
        }

        // タイムアウト設定　切り替えのみ即座の設定だから除外
        if (option != "change_publish_level") {
            Bukkit.getScheduler().runTaskLater(
                instance,
                Runnable {
                    timeOut()
                },
                120 * 20L
            )
        }
    }

    override fun open() {
        //
        owner.openInventory(ui)
    }

    override fun construct(): Inventory {
        val menu = createTemplate(5, "§8§lWorld Management")!!

        val playerData = PlayerData(owner)

        if (!world.isRegistered) throw IllegalArgumentException("Unregistered world for management ui specified.")

        // 総合情報
        val infoIcon = ItemStack(world.iconMaterial!!)
        infoIcon.itemMeta = infoIcon.itemMeta.apply {
            setItemName("§7【§a${world.name}§7】")
            lore = listOf(
                bar,
                "$index §7${world.description}",
                "$index §7拡張レベル §e§l${world.expansionLevel}§7/${Config.borderExpansionMax}",
                bar,
                "$index §7最終更新日時 §b${Lib.formatDate(world.lastUpdated!!)}",
                "$index §8§n${Lib.formatDate(world.expireDate!!)} §8に期限切れ (§8§n${
                    ChronoUnit.DAYS.between(LocalDate.now(), world.expireDate)
                }日後§8)",
                "$index §7メンバー §e${world.members!!.size}人 §7(オンライン: §a${world.members!!.filter { it.isOnline }.size}人§7)",
                "$index §7公開レベル §e${world.publishLevel!!.toJapanese()}",
                bar,
            )
        }

        // 設定ゾーン: 名前・説明
        val changeDisplayIcon = ItemStack(Material.NAME_TAG)
        changeDisplayIcon.itemMeta = changeDisplayIcon.itemMeta.apply {
            setItemName("§aワールド表示の変更")
            lore = listOf(
                bar,
                "§e左クリック§7: §aワールドの名前§7を変更します。",
                "§e右クリック§7: §aワールドの説明§7を変更します。",
                bar,
            )
        }

        // 設定ゾーン: アイコン
        val changeIconIcon = ItemStack(Material.ANVIL)
        changeIconIcon.itemMeta = changeDisplayIcon.itemMeta.apply {
            setItemName("§aワールド表示の変更")
            lore = listOf(
                bar,
                "§e左クリック§7: §aワールドのアイコン§7を変更します。",
                "§7インベントリ内のアイテムをクリックして、そのアイテムをアイコンに設定します。",
                bar,
            )
        }

        // 設定ゾーン: スポーン
        val changeSpawnIcon = ItemStack(Material.ENDER_PEARL)
        changeSpawnIcon.itemMeta = changeSpawnIcon.itemMeta.apply {
            setItemName("§aスポーン位置の変更")
            lore = listOf(
                bar,
                "§e左クリック§7: §aメンバーのスポーン位置§7を変更します。",
                "§e右クリック§7: §aゲストのスポーン位置§7を変更します。",
                bar,
            )
        }

        // 機能ゾーン: 拡張 TODO: 拡張のMAXに達したときの表示
        val expandIcon = ItemStack(Material.CHEST)
        expandIcon.itemMeta = expandIcon.itemMeta.apply {
            setItemName("§bワールドの拡張")
            lore = listOf(
                bar,
                "§fクリックして§bワールドのボーダー§fを§a1段階§f拡張します。",
                bar,
                "$index §7現在の拡張レベル §e§l${world.expansionLevel}§7/${Config.borderExpansionMax}",
                "$index §7コスト ${EmojiIconPrefix.WORLD_POINT} §e${world.expansionCost} §7(§7${world.expansionLevel} ➡ §e§l${
                    world.expansionLevel?.plus(
                        1
                    )
                })",
                bar,
            )
        }

        // 機能ゾーン: 公開レベル
        val publishLevelIcon = ItemStack(Material.COMPASS)
        publishLevelIcon.itemMeta = publishLevelIcon.itemMeta.apply {
            val list = PublishLevel.entries.map {
                if (it == world.publishLevel) "§f▶ §b ${it.toJapanese().drop(2)}" else "§7${it.toJapanese().drop(2)}"
            }

            setItemName("§bワールド公開レベルの変更")
            lore = listOf(
                bar,
                "§fクリックして§eワールドの公開レベルを変更§fします。",
                bar,
                list[0],
                list[1],
                list[2],
                bar,
            )
        }

        // 機能ゾーン: メンバー
        val memberIcon = ItemStack(Material.PLAYER_HEAD)
        memberIcon.itemMeta = memberIcon.itemMeta.apply {

            val memberList = world.members!!.map {
                when (it) {
                    world.owner -> "e${it.name} §f(§cOWNER§f)"
                    in world.moderators!! -> "§e${it.name} §f(§bMOD§f)"
                    else -> "§e${it.name}"
                }
            }

            setItemName("§bメンバー")
            lore = listOf(
                bar,
                "§fクリックしてほかのプレイヤーを§eメンバーに招待§fします。",
                bar,
                "$index §7現在の拡張レベル §e§l${world.expansionLevel}§7/${Config.borderExpansionMax}",
                "$index §7コスト ${EmojiIconPrefix.WORLD_POINT} §e${world.expansionCost} §7(§7${world.expansionLevel} ➡ §e${
                    world.expansionLevel?.plus(
                        1
                    )
                })",
                bar,
            ) + memberList + listOf(
                bar
            )
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
