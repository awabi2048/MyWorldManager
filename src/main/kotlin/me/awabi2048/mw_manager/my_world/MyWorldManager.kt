package me.awabi2048.mw_manager.my_world

import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.Main.Companion.mvWorldManager
import me.awabi2048.mw_manager.data_file.DataFiles
import org.bukkit.Bukkit
import org.bukkit.GameMode
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
            it.mvWorld!!.setGameMode(GameMode.SPECTATOR)
        }
    }

    fun updateData() {
        // どこにも存在しないワールドをworld_data.yml・MVから削除
        DataFiles.worldData.getKeys(false).filter{!MyWorld(it).isRealWorld}.forEach {
            DataFiles.worldData.set(it, null)
            mvWorldManager.removeWorldFromConfig("my_world.$it")
        }

        // プレイヤーデータのワープから存在しないワールドを削除
        DataFiles.playerData.getKeys(false).forEach { uuid ->
            String
            val shortcuts = DataFiles.playerData.getStringList("$uuid.warp_shortcuts")
            if (shortcuts.any { MyWorld(it).vanillaWorld == null }) {
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
