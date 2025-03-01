package me.awabi2048.mw_manager.macro_executor

import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.data_file.DataFiles
import me.awabi2048.mw_manager.macro_executor.MacroExecutor.Flag.*
import me.awabi2048.mw_manager.my_world.MyWorld
import org.bukkit.entity.Player

class MacroExecutor(private val flag: Flag) {
    enum class Flag {
        ON_WORLD_CREATE,
        ON_PLAYER_WARP,
    }

    private fun resolvePlaceholder(input: String, world: MyWorld, player: Player): String {
        return when(flag) {
            ON_WORLD_CREATE -> input
                .replace("%owner%", player.name)
                .replace("%world_uuid%", world.uuid)
                .replace("%world_name%", world.name!!)
                .replace("%template_name%", world.templateWorldName!!)

            ON_PLAYER_WARP -> input
                .replace("%player%", player.name)
                .replace("%world_uuid%", world.uuid)
        }
    }

    fun run(world: MyWorld, player: Player) {
        println("macro executor , ${flag.name.lowercase()}")

        DataFiles.macroSetting.getStringList(flag.name.lowercase()).forEach {
            println("command: $it")
        }

        val executeCommands = DataFiles.macroSetting.getStringList(flag.name.lowercase())
            .map {resolvePlaceholder(it, world, player)}

        executeCommands.forEach {
            println(it)
            instance.server.dispatchCommand(
                instance.server.consoleSender,
                it
            )
        }

    }
}
