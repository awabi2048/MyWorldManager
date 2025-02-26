package me.awabi2048.mw_manager.my_world

import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.data_file.DataFiles
import org.bukkit.Bukkit
import org.bukkit.WorldCreator
import java.io.File

object MyWorldManager {
    val registeredMyWorld: List<MyWorld>
        get() {
            return DataFiles.worldData.getKeys(false).map {
                MyWorld(it)
            }
        }

    val registeredTemplateWorld: List<TemplateWorld>
        get() {
            return DataFiles.templateSetting.getKeys(false).map { TemplateWorld(it) }
        }

    fun reloadPreviewWorld() {
        registeredTemplateWorld.forEach {
            // プレビュー用のワールドが用意されていないtemplateがあれば、ファイルコピー
            if (!File(Bukkit.getWorldContainer(), "preview.${it.worldId}").exists()) {
                val templateWorldFile =
                    File(Bukkit.getWorldContainer().parent, "template_worlds" + File.separator + it.worldId)
                val worldFolder = File(Bukkit.getWorldContainer(), "preview.${it.worldId}")

                templateWorldFile.copyRecursively(worldFolder)

                // ワールドとして認識してもらう
                Bukkit.createWorld(WorldCreator("preview.${it.worldId}"))
            }
        }
    }

    fun updateData() {
        // 存在しないワールドをworld_data.ymlから削除
        DataFiles.worldData.getKeys(false).forEach { uuid ->
            String
            if (Bukkit.getWorld("my_world.$uuid") == null) {
                instance.logger.info("Removed world data which its world does not exist from world_data.yml. (UUID: $uuid)")
                DataFiles.worldData.set(uuid, null)
            }
        }

        // プレイヤーデータのワープから存在しないワールドを削除
        DataFiles.playerData.getKeys(false).forEach { uuid ->
            String
            val shortcuts = DataFiles.playerData.getStringList("$uuid.warp_shortcuts")
            if (shortcuts.any { !MyWorld(it).isRegistered }) {
//                instance.logger.info("")
                shortcuts.removeIf { !MyWorld(it).isRegistered }
                DataFiles.playerData.set("$uuid.warp_shortcuts", shortcuts)
            }
        }

        //
        DataFiles.save()
    }

    fun updateTask(myWorld: MyWorld) {
        updateData()
        myWorld.sync()
    }
}
