package me.awabi2048.mw_manager.macro_executor

import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.data_file.DataFiles
import me.awabi2048.mw_manager.my_world.MyWorld
import org.bukkit.entity.Player

class MacroExecutor(private val flag: MacroFlag) {
    private fun resolvePlaceholder(input: String, world: MyWorld, player: Player): String {
        return when(flag) {
            MacroFlag.ON_WORLD_CREATE -> input
                .replace("%owner%", player.name)
                .replace("%world_uuid%", world.uuid)
                .replace("%world_name%", world.name!!)
                .replace("%template_name%", world.templateWorldName!!)

            MacroFlag.ON_PLAYER_WARP -> input
                .replace("%player%", player.name)
                .replace("%world_uuid%", world.uuid)
        }
    }

    fun run(world: MyWorld, player: Player) {
        val executeCommands = DataFiles.macroSetting.getStringList(flag.name.lowercase())
            .map {resolvePlaceholder(it, world, player)}

        executeCommands.forEach {
            instance.server.dispatchCommand(
                instance.server.consoleSender,
                it
            )
        }

    }
}
