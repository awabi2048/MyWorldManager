package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.creationDataSet
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.Main.Companion.mvWorldManager
import me.awabi2048.mw_manager.Main.Companion.prefix
import me.awabi2048.mw_manager.data_file.DataFiles
import me.awabi2048.mw_manager.my_world.*
import me.awabi2048.mw_manager.player_expansion.notify
import me.awabi2048.mw_manager.ui.WorldInfoListUI
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class MWMSubCommand(val sender: CommandSender, val args: Array<out String>) {
    fun reload() {
        DataFiles.copy()
        DataFiles.loadAll()

        sender.sendMessage("$prefix §aデータファイルをリロードしました。")
    }

    fun create() {
        if (args.size !in 3..4) {
            sender.notify("§c無効なコマンドです。", null)
            return
        }

        //
        val ownerSpecifier = args[1]
        val sourceWorldName = args[2]
        val worldName = args[3]

        val owner = Lib.translatePlayerSpecifier(ownerSpecifier)
        val uuid = UUID.randomUUID().toString()

        if (owner == null) {
            sender.notify("§c指定されたプレイヤーが見つかりません。", null)
            return
        }

        if (!mvWorldManager.mvWorlds.any { it.name == sourceWorldName }) {
            sender.notify("§c指定されーたワールドが見つかりません。", null)
            return
        }

        // register
        val myWorld = MyWorld(uuid)
        val initiated = myWorld.initiate(sourceWorldName, owner, worldName)

        if (initiated) {
            sender.notify(
                "Owner: ${owner.displayName}, Source: $sourceWorldName でワールドを生成しました。(UUID:$uuid)",
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
            val ui = WorldInfoListUI(sender, MyWorldManager.registeredMyWorld.toSet(), 1)
            ui.open(true)
        }

        // /mwm info %page%
        if (args.size == 2 && args[1].toIntOrNull() != null) {
            val page = args[1].toInt()
            val pageRange = 1.. MyWorldManager.registeredMyWorld.size / 36 + 1

            if (page !in pageRange) {
                sender.sendMessage("§c無効なページの指定です。")
                return
            }

            val ui = WorldInfoListUI(sender, MyWorldManager.registeredMyWorld.toSet(), page)
            ui.open(true)
        }

        // /mwm info %world%
        if (args.size == 2 && Lib.translateWorldSpecifier(args[1]) != null) {
            val world = Lib.translateWorldSpecifier(args[1])!!

            sender.sendMessage("§8【§a${world.name}§8】 §eのデータ")
            world.fixedData.forEach {
                sender.sendMessage(it)
            }

            return
        }

        // /mwm info %player%
        if (args.size == 2 && Lib.translatePlayerSpecifier(args[1]) != null) {
            val player = Lib.translatePlayerSpecifier(args[1])!!

            val ui = WorldInfoListUI(sender, MyWorldManager.registeredMyWorld.filter {it.members!!.contains(player)}.toSet(), 1)
            ui.open(true)
        }

        // /mwm info %player% %page%
        if (args.size == 3 && Lib.translatePlayerSpecifier(args[1]) != null && args[2].toIntOrNull() != null) {
            val player = Lib.translatePlayerSpecifier(args[1])!!
            val page = args[2].toInt()

            val pageRange = 1.. MyWorldManager.registeredMyWorld.size / 36 + 1

            if (page !in pageRange) {
                sender.sendMessage("§c無効なページの指定です。")
                return
            }

            val ui = WorldInfoListUI(sender, MyWorldManager.registeredMyWorld.filter {it.members!!.contains(player)}.toSet(), page)
            ui.open(true)
        }

        return
    }

    fun update() {
        if (args.size != 2) {
            sender.notify("§c無効なコマンドです。", null)
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
            sender.sendMessage("$prefix §c無効なコマンドです。")
            return
        }

        val world = Lib.translateWorldSpecifier(args[1])
        if (world == null) {
            sender.sendMessage("$prefix §c無効なワールド指定です。")
            return
        }

        if (world.activityState == state) {
            sender.sendMessage("§c指定されたワールドは、既に${state.toJapanese()}§c状態です。")
            return
        }

        // 実行
        world.activityState = state

        sender.sendMessage("$prefix §aワールド「${world.name}」を${state.toJapanese()}状態に設定しました。")
    }

    fun startCreationSession() {
        if (sender is Player) {
            if (creationDataSet.any {it.player == sender}) creationDataSet.removeIf {it.player == sender}

            val creationData = CreationData(sender, null, null, CreationStage.WORLD_NAME)
            creationDataSet += creationData

            // 最初 → ワールド名設定
            sender.sendMessage("$prefix §eワールド名§7を入力してください！")
            sender.playSound(sender, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f)

            // タイムアウト判定
            object: BukkitRunnable() {
                override fun run() {
                    // まだ作成手続き中ならキャンセル（5分）
                    if (creationDataSet.any {it.player == sender}) {
                        creationDataSet.removeIf {it.player == sender}
                        sender.sendMessage("§cセッションがタイムアウトしました。")
                        sender.closeInventory()
                    } else cancel()
                }
            }.runTaskLater(instance, 300 * 20)

        } else return
    }
}
