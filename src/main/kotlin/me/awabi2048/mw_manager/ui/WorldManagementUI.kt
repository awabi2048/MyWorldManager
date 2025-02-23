package me.awabi2048.mw_manager.ui

import me.awabi2048.mw_manager.EmojiIconPrefix
import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.config.Config
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.PublishLevel
import me.awabi2048.mw_manager.player_data.PlayerData
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.time.LocalDate

class WorldManagementUI(private val owner: Player, private val world: MyWorld) : AbstractUI(owner) {

    private val index = "§f§l|"


    override fun open() {
        //
        owner.openInventory(ui)
    }

    override fun construct(): Inventory {
        val menu = createTemplate(5, "§8§lWarp Management")!!

        val playerData = PlayerData(owner)

        if (!world.isRegistered) throw IllegalArgumentException("Unregistered world for management ui specified.")

        // 総合情報
        val infoIcon = ItemStack(world.iconMaterial!!)
        infoIcon.itemMeta = infoIcon.itemMeta.apply {
            setItemName("§7【§a${world.name}】§7")
            lore = listOf(
                bar,
                "$index §7${world.description}",
                "$index §7拡張レベル §e§l${world.expansionLevel}§7/${Config.borderExpansionMax}",
                bar,
                "$index §7最終更新日時 §b${Lib.formatDate(world.lastUpdated!!)}",
                "$index §8§n${Lib.formatDate(world.expireDate!!)}§8に期限切れ (§8§n${
                    world.expireDate!!.compareTo(
                        LocalDate.now()
                    )
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
                "$index §7コスト ${EmojiIconPrefix.WORLD_POINT} §e${world.expansionCost} §7(§7${world.expansionLevel} ➡ §e${
                    world.expansionLevel?.plus(
                        1
                    )
                })",
                bar,
            )
        }

        // 機能ゾーン: 公開レベル
        val publishLevelIcon = ItemStack(Material.CHEST)
        publishLevelIcon.itemMeta = publishLevelIcon.itemMeta.apply {
            val list = PublishLevel.entries.map {
                if (it == world.publishLevel) "§f▶ §b ${it.toJapanese().drop(2)}" else "§7${it.toJapanese().drop(2)}"
            }

            setItemName("§bワールドの更新")
            lore = listOf(
                bar,
                "§fクリックして§bワールドの公開レベルを変更§fします。",
                bar,
                list[0],
                list[1],
                list[2],
                bar,
            )
        }

        // 機能ゾーン: メンバー
        val memberIcon = ItemStack(Material.CHEST)
        memberIcon.itemMeta = memberIcon.itemMeta.apply {

            val memberList = world.members!!.map {
                when (it) {
                    world.owner -> "${it.name} §f(§cOWNER§f)"
                    in world.moderators!! -> "${it.name} §f(§bMOD§f)"
                    else -> it.name
                }
            }

            setItemName("§bメンバー")
            lore = listOf(
                bar,
                "§fクリックして§bワールドのボーダー§fを§a1段階§f拡張します。",
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
