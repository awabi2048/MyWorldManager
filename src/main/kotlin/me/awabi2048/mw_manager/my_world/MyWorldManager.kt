package me.awabi2048.mw_manager.my_world

import me.awabi2048.mw_manager.Main.Companion.mvWorldManager
import me.awabi2048.mw_manager.data_file.DataFiles
import org.bukkit.Bukkit
import org.bukkit.World
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

    fun loadTemplateWorlds() {
        registeredTemplateWorld.map {it.worldId}.forEach {
            Bukkit.createWorld(WorldCreator(it))
        }
        registeredTemplateWorld.forEach {
            it.cbWorld!!.entities.filter {it.scoreboardTags.contains("mwm.template_preview")}.forEach{it.remove()}
        }
    }

    fun updateData() {
        // 存在しないワールドをworld_data.yml・MVから削除
        DataFiles.worldData.getKeys(false).forEach { uuid ->
            String
            if (Bukkit.getWorld("my_world.$uuid") == null) {
                DataFiles.worldData.set(uuid, null)

                mvWorldManager.removeWorldFromConfig("my_world.$uuid")
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

    fun unloadMyWorlds() {
        registeredMyWorld.forEach {
            Bukkit.unloadWorld(it.vanillaWorld!!, false)
        }
    }
}
