package me.awabi2048.mw_manager.my_world

import me.awabi2048.mw_manager.Main.Companion.instance
import me.awabi2048.mw_manager.Main.Companion.mvWorldManager
import me.awabi2048.mw_manager.data_file.DataFiles
import me.awabi2048.mw_manager.portal.WorldPortal
import org.bukkit.Bukkit
import org.bukkit.WorldCreator

object MyWorldManager {
    val registeredMyWorlds: List<MyWorld>
        get() {
//            println(DataFiles.worldData.getKeys(false))

            return DataFiles.worldData.getKeys(false).map {
                MyWorld(it)
            }
        }

    val registeredTemplateWorld: List<TemplateWorld>
        get() {
            return DataFiles.templateSetting.getKeys(false).map { TemplateWorld(it) }
        }

    val registeredPortal: List<WorldPortal>
        get() {
            return DataFiles.portalData.getKeys(false).map {WorldPortal(it)}
        }

    fun loadTemplateWorlds() {
        registeredTemplateWorld.map {it.worldId}.forEach {
            Bukkit.createWorld(WorldCreator(it))
        }

        registeredTemplateWorld.forEach { templateWorld ->
            try {
                templateWorld.cbWorld!!.entities.filter { it.scoreboardTags.contains("mwm.template_preview") }
                    .forEach { it.remove() }

                val mvWorld = mvWorldManager.getMVWorld(templateWorld.cbWorld)
                val mvSpawnLocation = mvWorld.spawnLocation
                templateWorld.originLocation = mvSpawnLocation

            } catch (e: NullPointerException) {
                instance.logger.severe("template_setting.yml に記載されたテンプレートワールドが見つかりませんでした。(${templateWorld.worldId})")
                throw IllegalStateException("Template world data not found: ${templateWorld.worldId}")
            }
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

}
