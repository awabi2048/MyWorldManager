package me.awabi2048.mw_manager.command

import me.awabi2048.mw_manager.Lib
import me.awabi2048.mw_manager.Main
import me.awabi2048.mw_manager.Main.Companion.configData
import me.awabi2048.mw_manager.Main.Companion.mvWorldManager
import me.awabi2048.mw_manager.MyWorld
import me.awabi2048.mw_manager.MyWorldManager
import me.awabi2048.mw_manager.player_notify.notify
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import java.util.*

class SubCommand(val sender: CommandSender, val args: Array<out String>) {
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

        if (worldName in configData.getStringList("world_name_blacklist")) {
            sender.notify("§cそのワールド名は利用できません。", null)
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

            val worlds = MyWorldManager.registeredWorld.filter { it.owner == specifiedPlayer }

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
}
