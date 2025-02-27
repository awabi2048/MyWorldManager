package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main.Companion.creationDataSet
import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.Main.Companion.mvWorldManager
import me.awabi2048.mw_manager.Main.Companion.prefix
import me.awabi2048.mw_manager.data_file.DataFiles
import me.awabi2048.mw_manager.my_world.CreationData
import me.awabi2048.mw_manager.my_world.CreationStage
import me.awabi2048.mw_manager.my_world.MyWorld
import me.awabi2048.mw_manager.my_world.MyWorldManager
import me.awabi2048.mw_manager.player_expansion.notify
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class MWMSubCommand(val sender: CommandSender, val args: Array<out String>) {
    fun reload() {
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

        val owner = Lib.convertPlayerSpecifier(ownerSpecifier)
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
        if (args.size !in 2..3) {
            sender.notify("§c無効なコマンドです。", null)
            return
        }

        if (args[1].startsWith("world:") || args[1].startsWith("wuuid:")) {
            val worldSpecifier = args[1].substringAfter("world:")
            val specifiedWorld = Lib.translateWorldSpecifier(worldSpecifier)

            if (specifiedWorld == null) {
                sender.notify("§c指定されたワールドが見つかりませんでした。", null)
                return
            }

            specifiedWorld.fixedData.forEach {
                sender.sendMessage(it)
            }
        }

        if (args[1].startsWith("player:") || args[1].startsWith("puuid:")) {
            val playerSpecifier = args[1].substringAfter("player:")
            val specifiedPlayer = Lib.convertPlayerSpecifier(playerSpecifier)

            if (specifiedPlayer == null) {
                sender.notify("§c指定されたプレイヤーが見つかりませんでした。", null)
                return
            }

            val worlds = MyWorldManager.registeredMyWorld.filter { it.owner == specifiedPlayer }

            worlds.forEach {

            }
        }

        //
        val specifier = args[1]

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

    fun toggleActive() {

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
