package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.PREFIX
import me.awabi2048.mw_manager.Main.Companion.creationDataSet
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.Main.Companion.mvWorldManager
import me.awabi2048.mw_manager.custom_item.CustomItem
import me.awabi2048.mw_manager.data_file.Config
import me.awabi2048.mw_manager.data_file.DataFiles
import me.awabi2048.mw_manager.extension.notify
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.my_world.TemplateWorld
import me.awabi2048.mw_manager.my_world.world_create.CreationData
import me.awabi2048.mw_manager.my_world.world_create.CreationStage
import me.awabi2048.mw_manager.my_world.world_property.WorldActivityState
import me.awabi2048.mw_manager.player_data.PlayerData
import me.awabi2048.mw_manager.ui.top_level.AdminWorldInfoUI
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class MWMSubCommand(val sender: CommandSender, val args: Array<out String>) {
    fun reload() {
        DataFiles.copy()
        DataFiles.loadAll()

        MyWorldManager.loadTemplateWorlds()
        MyWorldManager.updateData()

        sender.sendMessage("$PREFIX §aデータファイルをリロードしました。")
    }

    fun create() {
        if (args.size != 4) {
            sender.notify("§c無効なコマンドです。 /mwm create <プレイヤー> <テンプレート> <ワールド名>", null)
            return
        }

        //
        val ownerSpecifier = args[1]
        val sourceWorldName = args[2]
        val worldName = args[3]
        val owner = Lib.translatePlayerSpecifier(ownerSpecifier)
        val uuid = UUID.randomUUID().toString()

        if (owner == null) {
            sender.notify("$PREFIX §c指定されたプレイヤーが見つかりません。", null)
            return
        }

        if (!mvWorldManager.mvWorlds.any { it.name == sourceWorldName }) {
            sender.notify("$PREFIX §c指定されたワールドが見つかりません。", null)
            return
        }

        if (!Lib.checkWorldNameAvailable(worldName, owner)) {
            sender.sendMessage("$PREFIX §cそのワールド名は使用できません。")
            return
        }

        // register
        val myWorld = MyWorld(uuid)
        val initiated = myWorld.initiate(sourceWorldName, owner, worldName)

        if (initiated) {
            sender.notify(
                "Owner: ${owner.name}, Source: $sourceWorldName でワールドを生成しました。(UUID:$uuid)",
                null
            )
        }
    }

    fun info() {
        // ワールド指定 → そのワールドのデータをチャット欄に表示
        // プレイヤー指定 → そのプレイヤーの所属するワールドを一覧表示
        // 指定なし → この世のすべてのワールドを一覧表示

        if (sender !is Player) {
            sender.sendMessage("§cこのコマンドはプレイヤーからのみ実行可能です。")
            return
        }

        // /mwm info
        if (args.size == 1) {
            val ui = AdminWorldInfoUI(sender, MyWorldManager.registeredMyWorlds.toSet(), 1)
            ui.open(true)
        }

        // /mwm info %page%
        if (args.size == 2 && args[1].toIntOrNull() != null) {
            val page = args[1].toInt()
            val pageRange = 1..MyWorldManager.registeredMyWorlds.size / 36 + 1

            if (page !in pageRange) {
                sender.sendMessage("§c無効なページの指定です。")
                return
            }

            val ui = AdminWorldInfoUI(sender, MyWorldManager.registeredMyWorlds.toSet(), page)
            ui.open(true)
        }

        // /mwm info %world%
        if (args.size == 2 && Lib.translateWorldSpecifier(args[1]) != null) {
            val world = Lib.translateWorldSpecifier(args[1])!!

            sender.sendMessage("§8【§a${world.name}§8】 §eのデータ")
            val lore = world.iconItem?.itemMeta?.lore()?.map { (it as TextComponent).content() }
            lore?.forEach {
                sender.sendMessage(it)
            }

            return
        }

        // /mwm info %player%
        if (args.size == 2 && Lib.translatePlayerSpecifier(args[1]) != null) {
            val player = Lib.translatePlayerSpecifier(args[1])!!

            val ui = AdminWorldInfoUI(
                sender,
                MyWorldManager.registeredMyWorlds.filter { it.members!!.contains(player) }.toSet(),
                1
            )
            ui.open(true)
        }

        // /mwm info %player% %page%
        if (args.size == 3 && Lib.translatePlayerSpecifier(args[1]) != null && args[2].toIntOrNull() != null) {
            val player = Lib.translatePlayerSpecifier(args[1])!!
            val page = args[2].toInt()

            val pageRange = 1..MyWorldManager.registeredMyWorlds.size / 36 + 1

            if (page !in pageRange) {
                sender.sendMessage("§c無効なページの指定です。")
                return
            }

            val ui = AdminWorldInfoUI(
                sender,
                MyWorldManager.registeredMyWorlds.filter { it.members!!.contains(player) }.toSet(),
                page
            )
            ui.open(true)
        }

        return
    }

    fun update() {
        if (args.size != 2) {
            sender.notify("§c無効なコマンドです。 /mwm update <ワールド>", null)
            return
        }

        val myWorld = Lib.translateWorldSpecifier(args[1])

        if (myWorld == null) {
            sender.notify("§c指定されたワールドが見つかりません。", null)
            return
        }

        myWorld.update()
        sender.notify("§a指定されたワールドを更新しました。", null)
    }

    fun setWorldState(state: WorldActivityState) {
        // コマンド有効判定
        if (args.size != 2) {
            sender.sendMessage("$PREFIX §c無効なコマンドです。 </mwm activate|archive <ワールド>")
            return
        }

        val world = Lib.translateWorldSpecifier(args[1])
        if (world == null) {
            sender.sendMessage("$PREFIX §c無効なワールド指定です。")
            return
        }

        if (world.activityState == state) {
            sender.sendMessage("§c指定されたワールドは、既に${state.japaneseName}§c状態です。")
            return
        }

        // 実行
        world.activityState = state

        sender.sendMessage("$PREFIX §aワールド「${world.name}」を${state.japaneseName}状態に設定しました。")
    }

    fun startCreationSession() {
        if (args.size != 2) {
            sender.sendMessage("$PREFIX §c無効なコマンドです。 /mwm start_creation_session <プレイヤー名>")
            return
        }

        val targetPlayer = Bukkit.getPlayer(args[1])
        if (targetPlayer == null) {
            sender.sendMessage("$PREFIX §cプレイヤーが見つかりませんでした。")
            return
        }

        if (creationDataSet.any { it.player.uniqueId == targetPlayer.uniqueId }) {
            targetPlayer.sendMessage("§c既にワールドを作成中です！")
            targetPlayer.playSound(targetPlayer, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f)
            return
        }

        val playerData = PlayerData(targetPlayer)

        // 個人の作成上限
        if (playerData.createdWorlds.size >= playerData.unlockedWorldSlot) {
            targetPlayer.sendMessage("§c既にワールドの作成数上限に達しています！ §7(${playerData.unlockedWorldSlot} 個中 ${playerData.createdWorlds.size} 個作成済み)")
            targetPlayer.playSound(targetPlayer, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f)
            return
        }

        // グローバル作成上限
        if (MyWorldManager.registeredMyWorlds.size >= Config.globalWorldCountMax) {
            targetPlayer.sendMessage("§cワールド数上限に達したため、新規作成は現在行えません。§c§nスタッフに連絡してください。")
            targetPlayer.playSound(targetPlayer, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f)
            targetPlayer.playSound(targetPlayer, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 0.5f)
            return
        }

        val sessionUUID = UUID.randomUUID()

        val creationData =
            CreationData(targetPlayer, null, null, CreationStage.WORLD_NAME, sessionUUID, targetPlayer.location)
        creationDataSet += creationData

        // 最初 → ワールド名設定
        targetPlayer.sendMessage("$PREFIX §eワールド名§7を入力してください！")
        targetPlayer.playSound(targetPlayer, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f)

        // タイムアウト判定
        object : BukkitRunnable() {
            override fun run() {
                // まだ作成手続き中ならキャンセル（5分）
                if (creationDataSet.any { it.player == sender && it.uuid == sessionUUID }) {
                    creationDataSet.removeIf { it.player == targetPlayer }
                    sender.sendMessage("§cセッションがタイムアウトしました。")
                    targetPlayer.closeInventory()
                } else cancel()
            }
        }.runTaskLater(instance, 300 * 20)
    }

    fun getItem() {
        if (sender !is Player) {
            sender.sendMessage("$PREFIX §cこのコマンドはプレイヤーからのみ実行可能です。")
            return
        }

        if (args.size !in 2..3) {
            sender.sendMessage("$PREFIX §c無効なコマンドです。 /mwm get_item <アイテムId> [プレイヤー]")
            return
        }

        if (args[1] !in CustomItem.entries.map { it.name }) {
            sender.sendMessage("$PREFIX §c無効なアイテムIdです。")
            return
        }

        // 自分にgive
        if (args.size == 2) {
            val item = CustomItem.valueOf(args[1])
            item.give(sender)
        }

        if (args.size == 3) {
            val player = Bukkit.getPlayer(args[2])
            if (player == null) {
                sender.sendMessage("$PREFIX §c無効なプレイヤーです。")
                return
            }

            val item = CustomItem.valueOf(args[1]).itemStack

            if (player.inventory.firstEmpty() == -1) {
                val itemEntity =  player.location.world.dropItem(player.location, item)
                itemEntity.velocity.zero()
            } else {
                player.inventory.addItem(item)
            }
        }
    }

    fun modifyPlayerData() {
        // /mwm player_data <player> <operation> <path> [value]
        if (args.size !in 4..5) {
            sender.sendMessage("$PREFIX §c無効なコマンドです。 /mwm player_data <プレイヤー> <操作> <パス> [値]")
            return
        }

        val player = Lib.translatePlayerSpecifier(args[1])
        val operation = args[2]
        val path = args[3]
        val value = if (args.size == 5) args[4].toIntOrNull() else null

        val availablePaths = listOf(
            "unlocked_world_slot",
            "unlocked_warp_slot",
            "world_point",
        )

        if (player == null) {
            sender.sendMessage("$PREFIX §cプレイヤーが見つかりませんでした。")
            return
        }

        if (operation !in listOf("add", "get", "subtract", "set")) {
            sender.sendMessage("$PREFIX §c無効なオプションです。")
            return
        }

        if (path !in availablePaths) {
            sender.sendMessage("$PREFIX §c無効なパスの指定です。")
            return
        }

        if (operation != "get" && (value == null || value < 0)) {
            sender.sendMessage("$PREFIX §c無効な値の指定です。この値は0以上である必要があります。")
            return
        }

        val playerData = PlayerData(player)
        val baseValue = when (path) {
            "unlocked_world_slot" -> playerData.unlockedWorldSlot
            "unlocked_warp_slot" -> playerData.unlockedWarpSlot
            "world_point" -> playerData.worldPoint
            else -> null
        }?: return

        if (operation == "get") {
            sender.sendMessage("$PREFIX §e${player.name} §7はデータ §a${path} §7は §b${baseValue} です。")
        } else {

            val modifiedValue = when(operation) {
                "add" -> baseValue + value!!
                "subtract" -> baseValue - value!!
                "set" -> value!!
                else -> null
            }?: return

            when (path) {
                "unlocked_world_slot" -> playerData.unlockedWorldSlot = modifiedValue
                "unlocked_warp_slot" -> playerData.unlockedWarpSlot = modifiedValue
                "world_point" -> playerData.worldPoint = modifiedValue
            }

            sender.sendMessage("$PREFIX §e${player.name} §7のデータ §a${path} §7を変更しました。")
        }
    }

    fun setupTemplate() {
        // /mwm setup_template <name> <description> <icon>
        if (sender !is Player) {
            sender.sendMessage("$PREFIX このコマンドはプレイヤーからの実行のみ有効です。")
            return
        }

        try {
            if (MyWorldManager.registeredTemplateWorld.any {it.worldId == args[1]}) {
                sender.sendMessage("$PREFIX §cそのテンプレートIDは既に使用されています。")
                return
            }

            if (!args[1].matches("^[a-zA-z0-9]*$".toRegex())) {
                sender.sendMessage("$PREFIX §cテンプレートIDには半角英数字のみ使用可能です。")
                return
            }

            val newTemplateWorld = TemplateWorld(args[1])
            newTemplateWorld.originLocation = sender.location
            newTemplateWorld.name = args[2]
            newTemplateWorld.description = args[3]

            MyWorldManager.loadTemplateWorlds()
            println(MyWorldManager.registeredTemplateWorld)

            sender.sendMessage("$PREFIX §e新しくテンプレートを登録しました。")
            sender.playSound(sender, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f)

        } catch (e: Exception) {
            sender.sendMessage("$PREFIX §c無効なコマンドです。/mwm setup_template <テンプレートID> <ワールド名> <説明>")
            return
        }
    }
}
