package me.awabi2048.mw_manager.macro_executor

import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.data_file.DataFiles
import me.awabi2048.mw_manager.macro_executor.MacroFlag.*

class MacroExecutor(private val flag: MacroFlag) {
    private fun resolvePlaceholder(input: String): String {
        return when (flag) {
            is OnWorldCreated -> input
                .replace("%owner%", flag.owner.name)
                .replace("%world_uuid%", flag.world.uuid)
                .replace("%world_name%", flag.world.name!!)
                .replace("%template_name%", flag.world.templateWorldName!!)

            is OnWorldWarp -> input
                .replace("%player%", flag.player.name)
                .replace("%world_uuid%", flag.world.uuid)

            is OnWorldMemberAdded -> input
                .replace("%player%", flag.addedPlayer.name!!)
                .replace("%world_uuid%", flag.world.uuid)

            is OnWorldMemberRemoved -> input
                .replace("%player%", flag.removedPlayer.name!!)
                .replace("%world_uuid%", flag.world.uuid)

            is OnWorldRemoved -> input
                .replace("%world_uuid%", flag.world.uuid)
        }
    }

    fun run() {
        // 登録されたコマンドからプレースホルダーを置換
        val key = flag.key
        val commands = DataFiles.macroSetting.getStringList(key)
            .map { resolvePlaceholder(it) }

        // 実行
        commands.forEach {
            instance.server.dispatchCommand(
                instance.server.consoleSender,
                it
            )
        }
    }
}
