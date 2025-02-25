package me.awabi2048.mw_manager.my_world

import me.awabi2048.mw_manager.Main.Companion.mvWorldManager
import me.awabi2048.mw_manager.config.DataFiles
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import org.bukkit.util.FileUtil
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

                // 起動後にコピーだと
                Bukkit.createWorld(WorldCreator("preview.${it.worldId}"))
            }
        }
    }
}
